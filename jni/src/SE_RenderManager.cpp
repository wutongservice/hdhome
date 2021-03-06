#include "SE_DynamicLibType.h"
#include "SE_RenderManager.h"
#include "SE_RenderUnit.h"
#include "SE_Camera.h"
#include "SE_Application.h"
#include "SE_Geometry3D.h"
#include "SE_Log.h"
#include "SE_ShaderProgram.h"
#include "SE_ResourceManager.h"
#include "SE_Layer.h"
#include "SE_Renderer.h"
#include "SE_RenderTarget.h"
#include "SE_ObjectManager.h"
#include "SE_RenderTargetManager.h"
#include <string.h>
#include "SE_BoundingVolume.h"
#include "SE_SceneManager.h"
#include "SE_DataValueDefine.h"
#include "SE_MemLeakDetector.h"

SE_RenderManager::SE_RenderManager()
{
	mCurrentScene = -1;
	mRenderSortType = SORT_BY_DISTANCE;
    mNeedBlurShadow = false;
    mIteratorChange = false;
}
struct _CompareRenderUnit
{
	SE_RenderManager::RENDER_SORT_TYPE type;
    bool operator()(SE_RenderUnit* right, SE_RenderUnit* left)
	{
		if(type == SE_RenderManager::SORT_BY_DISTANCE)
		{
            //near first draw
		    if(right->getDistanceToCamera() < left->getDistanceToCamera())
            {
			    return true;
            }
		    else
            {
			    return false;
		}
		}
		else if(type == SE_RenderManager::SORT_BY_RESOURCE)
		{
			SE_ProgramDataID leftProgramID = left->getShaderProgramID();
			SE_ProgramDataID rightProgramID = right->getShaderProgramID();
			SE_ImageDataID* leftImageDataArray = NULL;
			int leftImageDataArrayNum = 0;
			SE_ImageDataID* rightImageDataArray = NULL;
			int rightImageDataArrayNum = 0;
			const SE_Layer& leftLayer = left->getLayer();
			const SE_Layer& rightLayer = right->getLayer();
			if(leftLayer < rightLayer)
				return true;
			if(leftLayer > rightLayer)
				return false;
			if(leftProgramID < rightProgramID)
				return true;
			if(leftProgramID > rightProgramID)
				return false;
			left->getTexImageID(SE_TEXTURE0,leftImageDataArray, leftImageDataArrayNum);
			right->getTexImageID(SE_TEXTURE0,rightImageDataArray, rightImageDataArrayNum);
			if(leftImageDataArray == NULL && rightImageDataArray != NULL)
				return true;
			if(leftImageDataArray != NULL && rightImageDataArray == NULL)
				return false;
			if(leftImageDataArray == NULL && rightImageDataArray == NULL)
			{
				SE_ImageData** leftBaseColorImage = NULL;
				int leftBaseColorImageNum;
				SE_ImageData** rightBaseColorImage = NULL;
				int rightBaseColorImageNum;
				left->getTexImage(SE_TEXTURE0, leftBaseColorImage, leftBaseColorImageNum);
				right->getTexImage(SE_TEXTURE0, rightBaseColorImage, rightBaseColorImageNum);
				if(leftBaseColorImageNum > 0 && rightBaseColorImageNum == 0)
					return false;
				if(leftBaseColorImageNum == 0 && rightBaseColorImageNum > 0)
					return true;
				if(leftBaseColorImageNum == 0 && rightBaseColorImageNum == 0)
					return false;
				SE_ImageData* leftImageData = leftBaseColorImage[0];
				SE_ImageData* rightImageData = rightBaseColorImage[0];
				return leftImageData < rightImageData;
			}
			if(leftImageDataArray[0] < rightImageDataArray[0])
				return true;
			else
				return false;
		}
        return false;
	}
};

struct _CompareTransparencyRenderUnit
{
	SE_RenderManager::RENDER_SORT_TYPE type;
    bool operator()(SE_RenderUnit* right, SE_RenderUnit* left)
	{
		
	    if(right->getLayer().getLayer() < left->getLayer().getLayer())//small layer first
        {
		    return true;
        }
	    else if(right->getLayer().getLayer() == left->getLayer().getLayer())
        {

            SE_Spatial* renderSpatialRight = right->getRenderableSpatial();
            SE_Spatial* renderSpatialLeft = left->getRenderableSpatial();

            SE_Vector3f rightMin = right->getMinBounding();
            SE_Vector3f rightMax = right->getMaxBounding();

            SE_Vector3f leftMin = left->getMinBounding();
            SE_Vector3f leftMax = left->getMaxBounding();

            if(renderSpatialRight->isNeedBlendSortOnX())
            {
                if(leftMin.x > rightMax.x || (leftMin.x < rightMax.x && leftMax.x > rightMax.x))
                {
                    return true;
                }
                else
                {
                    return false;
                }

            }
            else if(renderSpatialRight->isNeedBlendSortOnY())
            {
                if(rightMin.y > leftMax.y || (rightMin.y < leftMax.y && rightMax.y > leftMax.y))
                {
                    return true;
                }
                else
                {
                    return false;
                }
            
            }
            else
            {
                if(leftMin.z > rightMax.z || (leftMin.z < rightMax.z && leftMax.z > rightMax.z))
                {
                    return true;
                }
                else
                {
                    return false;
                }
            }
        }

#if 0
            //far first draw
		    if(left->getDistanceToCamera() < right->getDistanceToCamera())
	{
			    return true;
            }
		    else
		{
			    return false;
            }
		}
#endif
        return false;
    }

};
void SE_RenderManager::sort()
{

	_CompareRenderUnit cru;
    _CompareTransparencyRenderUnit ctru;
	cru.type = mRenderSortType;
    ctru.type = mRenderSortType;
    for(int i = 0 ; i < SE_MAX_SCENE_SIZE ; i++)
    {
        _SceneUnit* sceneUnit = &mSceneUnits[i];
        if(/*sceneUnit->mRenderTarget == NULL ||*/ sceneUnit->mCamera == NULL)
			continue;
        for(int j = 0 ; j < sceneUnit->mRenderQueue.size() ; j++)
        {
            RenderUnitList* ruList = &sceneUnit->mRenderQueue[j];
            if(ruList->size() == 0)
            {
                continue;
            }
            if(j == RQ8 || j == RQ9 || j== RQ10)
            {
                //RQ8 - 10 list is for transparency object draw,the draw sequence is from far to near.
                ruList->sort(ctru);
            }
            else
            {
            ruList->sort(cru);
        } 
    }

}

}
void SE_RenderManager::clear()
{
    for(int i = 0 ; i < SE_MAX_SCENE_SIZE ; i++)
    {
        _SceneUnit* sceneUnit = &mSceneUnits[i];
        sceneUnit->mCamera = NULL;
        //sceneUnit->mRenderTarget = NULL;
        sceneUnit->mNeedClear = false;
        for(int j = 0 ; j < sceneUnit->mRenderQueue.size() ; j++)
        {
            RenderUnitList* ruList = &sceneUnit->mRenderQueue[j];

#ifndef USE_RUMANAGER
            //render unit manager do this
            RenderUnitList::iterator it;
			for(it = ruList->begin() ; it != ruList->end() ;it++)
			{
				SE_RenderUnit* ru = *it;
				delete ru;
			}
#endif
			ruList->clear();
        }
    }



    for(int i = 0; i < mMirrorGroup.size(); ++i)
    {
        _MirrorGroupElement* e =  mMirrorGroup[i];
        e->mRuList.clear();//clear mirror group
    }

    mMirrorRUObjectList.clear();
        
    /*
	std::list<_RenderTargetUnit*>::iterator it;
	for(it = mRenderTargetList.begin() ; it != mRenderTargetList.end() ; it++)
	{
		_RenderTargetUnit* rt = *it;
		if(rt)
		{
			for(int i = 0 ; i  < RQ_NUM ; i++)
			{
				RenderUnitList* ruList = rt->mRenderQueue[i];
				RenderUnitList::iterator it;
				for(it = ruList->begin() ; it != ruList->end() ;it++)
				{
					SE_RenderUnit* ru = *it;
					delete ru;
				}
				ruList->clear();
				delete ruList;
			}
			delete rt;
		}
	}
    mRenderTargetList.clear();
    */
}
SE_RenderManager::~SE_RenderManager()
{
    clear();
    for(int i = 0; i < mMirrorGroup.size(); ++i)
    {
        _MirrorGroupElement* e =  mMirrorGroup[i];
        delete e;
    }
    mMirrorGroup.clear();

    removeAllMirrorGenerator();
    removeAllShadowGenerator();
}
void SE_RenderManager::beginDraw()
{
    clear();
	SE_Application::getInstance()->getStatistics().clear();

    SE_RenderTargetManager* rtm = SE_Application::getInstance()->getRenderTargetManager();
if(mNeedBlurShadow)
    {
        SE_RenderTargetManager* rtm = SE_Application::getInstance()->getRenderTargetManager();
        //down sample shadow map
        SE_TriSurfaceRenderUnit* downsample = new SE_TriSurfaceRenderUnit();
        downsample->setShaderName(DOWNSAMPLE_SHADER);
        downsample->setRenderName(DOWNSAMPLE_RENDERER);
        downsample->setPrimitiveType(TRIANGLE_STRIP);
        downsample->setNeedDownSample(true);
        downsample->setRenderToFbo(true);
        this->addRenderUnit(downsample,SE_RenderManager::RQ1); 
        downsample->setSourceRenderTarget(rtm->getDofGenTarget());//set shadow target as blurv source 


        //for blur shadow
        SE_TriSurfaceRenderUnit* blurH = new SE_TriSurfaceRenderUnit();
        SE_TriSurfaceRenderUnit* blurV = new SE_TriSurfaceRenderUnit();

        blurH->setNeedBlurHShadow(true);
        blurV->setNeedBlurVShadow(true);

        blurH->setRenderToFbo(true);
        blurV->setRenderToFbo(true);            

        if(1)
        {
            blurH->setShaderName(BLUR_SHADER);
            blurH->setRenderName(BLUR_RENDERER);

            blurV->setShaderName(BLUR_SHADER);
            blurV->setRenderName(BLUR_RENDERER);
        }
        else
        {
        blurH->setShaderName(BLURH_SHADER);
        blurH->setRenderName(BLURH_RENDERER);

        blurV->setShaderName(BLURV_SHADER);
        blurV->setRenderName(BLURV_RENDERER);
        }

        blurH->setPrimitiveType(TRIANGLE_STRIP);
        blurV->setPrimitiveType(TRIANGLE_STRIP);

        //first-v then h
        this->addRenderUnit(blurV,SE_RenderManager::RQ1); 
        this->addRenderUnit(blurH,SE_RenderManager::RQ2); 


        //draw final quad to default target
        SE_TriSurfaceRenderUnit* drawquad = new SE_TriSurfaceRenderUnit();
        drawquad->setShaderName(DRAWDOF_SHADER);
        drawquad->setRenderName(DRAWDOF_RENDERER);
        drawquad->setPrimitiveType(TRIANGLE_STRIP);
        drawquad->setNeedDrawQuad(true);
        drawquad->setRenderToFbo(true);
        this->addRenderUnit(drawquad,SE_RenderManager::RQ8); 
        drawquad->setSourceRenderTarget(rtm->getDofGenTarget());//set high res texture
    }

#if 0
    //draw target to screen
    if(this->getCurrentScene() != -1)
    {
        SE_TriSurfaceRenderUnit* drawscreen = new SE_TriSurfaceRenderUnit();
        drawscreen->setShaderName(DRAWRTTOSCREEN_SHADER);
        drawscreen->setRenderName(DRAWRTTOSCREEN_RENDERER);
        drawscreen->setPrimitiveType(TRIANGLE_STRIP);    
        drawscreen->setRenderToFbo(false);
        this->addRenderUnit(drawscreen,SE_RenderManager::RQ14); 
        drawscreen->setSourceRenderTarget(rtm->getDrawScreenToTarget());//set high res texture
    }
#endif
}
void SE_RenderManager::endDraw()
{
    //clear some status list
    this->clearRenderTargetCreatorTransform();

    //all mirror generator has draw finish
    mMirrorGeneratorCount.clear();

}
void SE_RenderManager::draw()
{
#if 0
    for(int i = 0; i < mMirrorGroup.size(); ++i)
    {
         _MirrorGroupElement* e =  mMirrorGroup[i];
         for(int j = 0; j < e->mRuList.size(); ++j)
         {
             SE_RenderUnit* ru = e->mRuList[j];
             this->addRenderUnit(ru,SE_RenderManager::RQ2);
         }
    }
#else

    drawMirrorGenerator();
#endif
#ifdef DEBUG
    int renderUnitNum = 0;
#endif
    bool preDepthState = false;
    bool preBlendState = false;


    SE_Renderer::clear(SE_Renderer::SE_COLOR_BUFFER);
    SE_Renderer::setClearColor(SE_Vector4f(0.0, 0.0, 0.0, 0.0));
    SE_Renderer::clear(SE_Renderer::SE_DEPTH_BUFFER);

    for(int i = 0 ; i < SE_MAX_SCENE_SIZE ; i++)
    {
        //1.got scene unit
        _SceneUnit* sceneUnit = &mSceneUnits[i];
        
        bool needClear = sceneUnit->mNeedClear;
		
        //2. set draw matrix
        SE_Camera* camera = sceneUnit->mCamera;
        if(camera == NULL)
        {
            //LOGI("render target is null or camera is null, not draw this scene\n");
            continue;
        }
        
        SE_Matrix4f m= camera->getPerspectiveMatrix().mul(camera->getWorldToViewMatrix());
        
		if(camera->getType() != SE_Camera::EPerspective)
		{
			m = camera->getOrthoMatrix().mul(camera->getWorldToViewMatrix());
		}

        SE_Camera* helpCamera = sceneUnit->mHelperCamera;//now for shadow

        //3.update view port
		SE_Rect<int> rect = camera->getViewport();
		if((rect.right - rect.left) == 0 || (rect.bottom - rect.top) == 0)
        {
			continue;
        }
        SE_Application::getInstance()->getRenderTargetManager()->getDefaultRenderTarget()->setWidth(rect.right - rect.left);
        SE_Application::getInstance()->getRenderTargetManager()->getDefaultRenderTarget()->setHeight(rect.bottom - rect.top);
        

        //4.refresh screen
        if(needClear)
        {
			SE_Renderer::setClearColor(sceneUnit->mBgColor);
			SE_Renderer::clear(SE_Renderer::SE_COLOR_BUFFER);
            SE_Renderer::setClearColor(SE_Vector4f(0.0,0.0, 0.0, 1.0));
                        SE_Renderer::clear(SE_Renderer::SE_DEPTH_BUFFER);

        }

        //5.process render unit list
        for(int j = 0 ; j < sceneUnit->mRenderQueue.size() ; j++)
        {
			RenderUnitList* ruList = &sceneUnit->mRenderQueue[j];

            if(ruList->size() == 0)
            {
                continue;
            }
			RenderUnitList::iterator it;

            SE_RenderUnit* ru = NULL;

            //6.process render unit
			for(it = ruList->begin() ; it != ruList->end(); it++)
			{

				ru = *it;
                //blendru = ru;

				ru->setViewToPerspectiveMatrix(m);

                ru->setHelperCamera(helpCamera);

                //7.update blend and depth status
                //render unit will set blend status when created.                
                if(preDepthState != ru->isEnableDepthTest() || preBlendState != ru->isEnableBlend())
                {
                    //the render state has changed,need apply,otherwise do not need change opengl state
				ru->applyRenderState();
                }

                
                SE_RenderTarget* rendertarget = ru->getRenderTarget();
                //8.update fbo status
                if(!rendertarget)
                {
                    SE_RenderTargetManager* rtm = SE_Application::getInstance()->getRenderTargetManager();

                    //if render shadow,this render target do not render to other fbo.
                    if(ru->isNeedGenerateShadow())
                    {
                        SE_Renderer::setClearColor(SE_Vector4f(1.0,1.0, 1.0, 1.0));
                        rendertarget = rtm->getShadowTarget();
                        if(rendertarget)
                        {                            

                            if(1)
                            {
                                //vsm
                            rendertarget->setRenderTargetAttribute(SE_RenderTarget::RT_DEPTHBUFFER);
                            rendertarget->setRenderTargetAttribute(SE_RenderTarget::RT_COLOR_TEXTURE);   
                            //rendertarget->setRenderTargetAttribute(SE_RenderTarget::RT_MIPMAP_ATTR);   
                                rendertarget->setWidth(1024);
                                rendertarget->setHeight(1024);
                            }
                            else
                            {
                                rendertarget->setRenderTargetAttribute(SE_RenderTarget::RT_DEPTH_TEXTURE);  
                                rendertarget->setWidth(1024);
                                rendertarget->setHeight(1024);
                                //((SE_TextureTarget*)rendertarget)->setSampleMin(NEAREST);
                                //((SE_TextureTarget*)rendertarget)->setSampleMag(NEAREST);
                            }

                            ((SE_TextureTarget*)rendertarget)->setWrapS(CLAMP);
                            ((SE_TextureTarget*)rendertarget)->setWrapT(CLAMP);


                            rendertarget->create();
                            
                        }
                    }
                    else if(ru->isNeedDofGen())
                    {
                        SE_Renderer::setClearColor(SE_Vector4f(0.0,0.0, 0.0, 1.0));
                        rendertarget = rtm->getDofGenTarget();
                        if(rendertarget)
                        {
                            //draw dof para
                            rendertarget->setRenderTargetAttribute(SE_RenderTarget::RT_DEPTHBUFFER);
                            rendertarget->setRenderTargetAttribute(SE_RenderTarget::RT_COLOR_TEXTURE);   
                            //rendertarget->setRenderTargetAttribute(SE_RenderTarget::RT_MIPMAP_ATTR);   
                            rendertarget->setWidth(1024);
                            rendertarget->setHeight(1024);
                            

                            ((SE_TextureTarget*)rendertarget)->setWrapS(CLAMP);
                            ((SE_TextureTarget*)rendertarget)->setWrapT(CLAMP);

                            rendertarget->create();
                            
                        }
                    }
                    //if render mirror,this render target do not render to other fbo.
                    else if(ru->isNeedGenerateMirror())
                    {
                        SE_Renderer::setClearColor(SE_Vector4f(1.0,1.0, 1.0, 1.0));
                        SE_Spatial* mirror = ru->getMirrorObject();
                        if(mirror)
                        {                        
                            rendertarget = rtm->get(mirror->getSpatialName());
                            if(rendertarget)
                            {                             
                                rendertarget->setRenderTargetAttribute(SE_RenderTarget::RT_DEPTHBUFFER);
                                rendertarget->setRenderTargetAttribute(SE_RenderTarget::RT_COLOR_TEXTURE);

                                rendertarget->setWidth(1024);
                                rendertarget->setHeight(1024);
                                rendertarget->create();                      
                                
                            }
                            else
                            {
                                rendertarget = new SE_TextureTarget();
                                rendertarget->setRenderTargetAttribute(SE_RenderTarget::RT_DEPTHBUFFER);
                                rendertarget->setRenderTargetAttribute(SE_RenderTarget::RT_COLOR_TEXTURE);

                                rendertarget->setWidth(1024);
                                rendertarget->setHeight(1024);
                                rendertarget->create();

                                if(!rtm->set(mirror->getSpatialName(),rendertarget))
                                {
                                    LOGI("render target has exist!!!\n");
                                    delete rendertarget;
                                }

                            }
                        }
                        else
                        {
                            if(SE_Application::getInstance()->SEHomeDebug)
                            LOGI("Error,mirror object not found!!\n");
                        }
                    }
                    else if (ru->isNeedDownSample())
                    {
                        rendertarget = rtm->getDownSampleTarget();
                        if(rendertarget)
                        {                            
                            rendertarget->setRenderTargetAttribute(SE_RenderTarget::RT_COLOR_TEXTURE);

                            rendertarget->setWidth(256);
                            rendertarget->setHeight(256);
                            rendertarget->create();                            
                            
                        }
                    }
                    else if(ru->isNeedBlurVShadow())
                    {
                        rendertarget = rtm->getBlurVTarget();
                        if(rendertarget)
                        {                            
                            rendertarget->setRenderTargetAttribute(SE_RenderTarget::RT_COLOR_TEXTURE);

                            rendertarget->setWidth(256);
                            rendertarget->setHeight(256);
                            rendertarget->create();                       
                            ru->setSourceRenderTarget(rtm->getDownSampleTarget());//set shadow target as blurv source 
                            
                        }
                    }
                    else if(ru->isNeedBlurHShadow())
                    {
                        rendertarget = rtm->getBlurHTarget();
                        if(rendertarget)
                        {                            
                            rendertarget->setRenderTargetAttribute(SE_RenderTarget::RT_COLOR_TEXTURE);

                            rendertarget->setWidth(256);
                            rendertarget->setHeight(256);
                            rendertarget->create();                       
                            ru->setSourceRenderTarget(rtm->getBlurVTarget());//set blurv as blurh source 
                            
                        }
                    }                    
                    else if(ru->isNeedDrawQuad())
                    {
                        rendertarget = rtm->getDefaultRenderTarget();

                    }       
                    else if(ru->isNeedColorEffect())
                    {
                        SE_Renderer::setClearColor(SE_Vector4f(0.0, 0.0, 0.0, 1.0));
                        rendertarget = rtm->getColorEffectTarget();
                        rendertarget->setRenderTargetAttribute(SE_RenderTarget::RT_DEPTHBUFFER);
                        rendertarget->setRenderTargetAttribute(SE_RenderTarget::RT_COLOR_TEXTURE);
                        rendertarget->setWidth(rect.right - rect.left);
                        rendertarget->setHeight(rect.bottom - rect.top);
                        rendertarget->create();
                    }
                    else if(ru->isRenderToFbo())
                    {
                        //set draw all to draw screen target,now just for demo    
                        rendertarget = rtm->getDrawScreenToTarget();
                        if(rendertarget)                        
                        {
                            rendertarget->setRenderTargetAttribute(SE_RenderTarget::RT_DEPTHBUFFER);
                            rendertarget->setRenderTargetAttribute(SE_RenderTarget::RT_COLOR_TEXTURE);
                            rendertarget->setWidth(rect.right - rect.left);
                            rendertarget->setHeight(rect.bottom - rect.top);
                            rendertarget->create();
                        }
                    }
                    else
                    {
                        rendertarget = rtm->getDefaultRenderTarget();
                    }

                    if(rendertarget)
                    {
                    ru->setRenderTarget(rendertarget);
                }
                }

                //9.update render target status
                
                if(rendertarget)
                {
                    if(rendertarget == SE_Application::getInstance()->getRenderTargetManager()->getCurrentBoundTarget())
                    {
                        glViewport(0, 0,rendertarget->getWidth(),rendertarget->getHeight());
                        //current target has been bound,do nothing
                    }
                    else
                    {
                        //the rt need prepare
                        if(!rendertarget->prepare())
                        {
                            continue;
                        }
                    }
                }
                else
                {
                    if(SE_Application::getInstance()->SEHomeDebug)
                    LOGI("### Error!!,no default render target!!!!!!!####\n\n.");
                    
                }

                //11.draw                
				ru->draw();

                //12.save blend and depth 
                //save render state
                preDepthState = ru->isEnableDepthTest();
                preBlendState = ru->isEnableBlend();

#ifdef DEBUG
		        renderUnitNum++;
#endif
            }

            //13.disable gl_blend when draw RQ8-10 list finish.
            if(ru)
            {
                if(j == (int)RQ10)
                {                    
                    ru->setBlendState(false);
                    ru->applyRenderState();
                    preBlendState = false;
                }
            }
        }
    }

    //_forwardAdditiveDraw();
    /*
	if(mRenderTargetList.empty())
		return;
    SE_Matrix4f m;
	std::list<_RenderTargetUnit*>::iterator it = mRenderTargetList.begin();
	std::list<_RenderTargetUnit*>::iterator startIt = mRenderTargetList.begin();
	it++;
	SE_RenderTargetManager* renderTargetManager = SE_Application::getInstance()->getRenderTargetManager();
	for(; it != mRenderTargetList.end() ; it++)
	{
		_RenderTargetUnit* rt = *it;
		SE_RenderTarget* renderTarget = renderTargetManager->getRenderTarget(rt->mRenderTargetID);
		SE_Camera* camera = renderTarget->getCamera();
        if(camera)
		{
			m = camera->getPerspectiveMatrix().mul(camera->getWorldToViewMatrix());
		}
		for(int i = 0 ; i < RQ_NUM ; i++)
		{
			RenderUnitList* ruList = rt->mRenderQueue[i];
			RenderUnitList::iterator it;
			for(it = ruList->begin() ; it != ruList->end() ;it++)
			{
				SE_RenderUnit* ru = *it;
				ru->setViewToPerspectiveMatrix(m);
				ru->applyRenderState();
				ru->draw();
			}
		}
	}
	if(startIt != mRenderTargetList.end())
	{
        _RenderTargetUnit* rt = *startIt;
		SE_Camera* currCamera = SE_Application::getInstance()->getCurrentCamera();
        SE_Rect<int> rect = currCamera->getViewport();
	    SE_Renderer::setViewport(0, 0, rect.right - rect.left, rect.bottom - rect.top);
	    SE_Renderer::setClearColor(SE_Vector4f(mBackground.x, mBackground.y, mBackground.z, 1.0));
	    SE_Renderer::clear(SE_Renderer::SE_COLOR_BUFFER | SE_Renderer::SE_DEPTH_BUFFER);
		m = mPerspectiveMatrix.mul(mWorldToViewMatrix);
		for(int i = 0 ; i < RQ_NUM ; i++)
		{
			RenderUnitList* ruList = rt->mRenderQueue[i];
			RenderUnitList::iterator it;
			for(it = ruList->begin() ; it != ruList->end() ;it++)
			{
				SE_RenderUnit* ru = *it;
				ru->setViewToPerspectiveMatrix(m);
				ru->applyRenderState();
				ru->draw();
			}
		}
	}
    */
}

/*
SE_RenderManager::_RenderTargetUnit* SE_RenderManager::findTarget(const SE_RenderTargetID& id)
{
	std::list<_RenderTargetUnit*>::iterator it;
	for(it = mRenderTargetList.begin() ; it != mRenderTargetList.end() ; it++)
	{
        _RenderTargetUnit* rt = *it;
		if(rt->mRenderTargetID == id)
		{
			return rt;
		}
	}
	return NULL;

}
*/
void SE_RenderManager::addRenderUnit(SE_RenderUnit* ru, RENDER_QUEUE rq)
{
    _SceneUnit* sceneUnit = &mSceneUnits[mCurrentScene];
    RenderUnitList* ruList = &sceneUnit->mRenderQueue[rq];
    ruList->push_back(ru);
    
    if(ru->isNeedRenderMirror())
	{
        mMirrorRUObjectList.push_back(ru);
	}
}
/*
void SE_RenderManager::setCurrentRenderTarget(SE_RenderTarget* renderTarget)
{
    _SceneUnit* sceneUnit = &mSceneUnits[mCurrentScene];
    sceneUnit->mRenderTarget = renderTarget;
}
SE_RenderTarget* SE_RenderManager::getCurrentRenderTarget() const
{
    const _SceneUnit* sceneUnit = &mSceneUnits[mCurrentScene];
    return sceneUnit->mRenderTarget;
}
*/

void SE_RenderManager::setCurrentSceneNeedClear(bool need)
{
    _SceneUnit* sceneUnit = &mSceneUnits[mCurrentScene];
    sceneUnit->mNeedClear = need;
}
bool SE_RenderManager::isCurrentSceneNeedClear() const
{
    const _SceneUnit* sceneUnit = &mSceneUnits[mCurrentScene];
    return sceneUnit->mNeedClear;
}

void SE_RenderManager::setCurrentCamera(SE_Camera* camera)
{
    _SceneUnit* sceneUnit = &mSceneUnits[mCurrentScene];
    sceneUnit->mCamera = camera;

}
SE_Camera* SE_RenderManager::getCurrentCamera() const
{
    const _SceneUnit* sceneUnit = &mSceneUnits[mCurrentScene];
    return sceneUnit->mCamera;
}

void SE_RenderManager::setCurrentHelperCamera(SE_Camera* camera)
{
    _SceneUnit* sceneUnit = &mSceneUnits[mCurrentScene];
    sceneUnit->mHelperCamera = camera;

}
SE_Camera* SE_RenderManager::getCurrentHelperCamera() const
{
    const _SceneUnit* sceneUnit = &mSceneUnits[mCurrentScene];
    return sceneUnit->mHelperCamera;
}

void SE_RenderManager::setCurrentBackgroundColor(const SE_Vector4f& c)
{
    _SceneUnit* sceneUnit = &mSceneUnits[mCurrentScene];
    sceneUnit->mBgColor = c;
}
SE_Vector4f SE_RenderManager::getCurrentBackgroundColor() const
{
    const _SceneUnit* sceneUnit = &mSceneUnits[mCurrentScene];
    return sceneUnit->mBgColor;
}
void SE_RenderManager::addRenderUnit(SE_RenderUnit *ru, const char *mirrorName)
{
    for(int i = 0; i < mMirrorGroup.size(); ++i)
    {
        _MirrorGroupElement* e =  mMirrorGroup[i];
        if(e->mGroupName.compare(mirrorName) == 0)
        {
            e->mRuList.push_back(ru);
            return;
        }        
    }
    
    _MirrorGroupElement* newgroup = new _MirrorGroupElement();
    newgroup->mGroupName = mirrorName;
    newgroup->mRuList.push_back(ru);
    mMirrorGroup.push_back(newgroup);

}


void SE_RenderManager::insertMirrorGenerator(SE_Spatial *spatial)
{

    SpatialList::iterator it = mMirrorGeneratorCount.begin();
    for(;it != mMirrorGeneratorCount.end();it++)
    {
        if( (*it) == spatial)
    {
        return;
    }
    }

    mIteratorChange = true;
    mMirrorGeneratorCount.push_back(spatial);    
}

void SE_RenderManager::insertShadowGenerator(const char *spatialName, SE_Spatial *spatial)
{
    std::string name(spatialName);
    SpatialMap::iterator it = mShadowGeneratorCount.find(name);
    if(it != mShadowGeneratorCount.end())
    {
        if(SE_Application::getInstance()->SEHomeDebug)
        LOGI("insert shadow generator[%s] fail,the spatial has been in list.\n\n",spatialName);
        return;
    }
    mShadowGeneratorCount.insert(std::pair<std::string,SE_Spatial*> (name,spatial));
    if(SE_Application::getInstance()->SEHomeDebug)
    LOGI("insert shadow generator[%s] success! there are [%d] shadow generators.\n\n",spatialName,mShadowGeneratorCount.size());
}

void SE_RenderManager::removeMirrorGenerator(SE_Spatial* spatial)
{
    
    SpatialList::iterator it = mMirrorGeneratorCount.begin();
    for(it = mMirrorGeneratorCount.begin();it != mMirrorGeneratorCount.end();)
    {
        if( (*it) == spatial)
    {        
        mMirrorGeneratorCount.erase(it);
            break;
        }
        else
            {
            it++;
        }
    }

    mIteratorChange = true;
    
}

void SE_RenderManager::removeShadowGenerator(const char *spatialName)
{
    std::string name(spatialName);
    SpatialMap::iterator it = mShadowGeneratorCount.find(name);
    if(it != mShadowGeneratorCount.end())
    {
        mShadowGeneratorCount.erase(it);
        if(SE_Application::getInstance()->SEHomeDebug)
        LOGI("remove shadow generator[%s] success! there are [%d] shadow generators.\n\n",spatialName,mShadowGeneratorCount.size());
    }
}

void SE_RenderManager::clearSpatialStatus(SE_Spatial* spatial)
{
    removeMirrorGenerator(spatial);
    removeShadowGenerator(spatial->getSpatialName());
}

void SE_RenderManager::removeAllMirrorGenerator()
{
    mMirrorGeneratorCount.clear();
}
void SE_RenderManager::removeAllShadowGenerator()
{
    mShadowGeneratorCount.clear();
}
void SE_RenderManager::mirrorRenderChange(SE_Spatial *mirrorRender)
{
    SpatialList::iterator it = mMirrorGeneratorCount.begin();

    for(;it != mMirrorGeneratorCount.end();it++)
    {
        SE_Spatial* sp = *it;
        if(sp->getMirrorObject() == mirrorRender)
        {
            sp->clearMirror();
        }

    }
}
void SE_RenderManager::check()
{

}

void SE_RenderManager::drawMirrorGenerator()
{
    RenderUnitList::iterator mirrorIt = mMirrorRUObjectList.begin();

    SE_Camera* c = this->getCurrentCamera();
    bool cameraChanged = c->isChanged();
    bool needRender = isNeedUpdateMirrorMap();

    if(cameraChanged || needRender)
    {
        for(;mirrorIt != mMirrorRUObjectList.end(); mirrorIt++)
        {

            SE_Spatial* mirror = (*mirrorIt)->getRenderableSpatial();

            SpatialList::iterator it = mMirrorGeneratorCount.begin();
        for(;it != mMirrorGeneratorCount.end(); it++)
        {
                if(mIteratorChange)
                {
                    it = mMirrorGeneratorCount.begin();
                    mIteratorChange = false;
                }
                SE_Spatial* generator = *it;            
                
                generator->renderSpatialToTarget(c,this,NULL,SE_PART_CULL,mirror);
            }
        }
    }
}
