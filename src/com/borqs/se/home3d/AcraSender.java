//package com.borqs.se.home3d;
//
//import java.io.BufferedReader;
//import java.io.FileNotFoundException;
//import java.io.FileReader;
//import java.io.IOException;
//
//import org.acra.ReportField;
//import org.acra.collector.CrashReportData;
//import org.acra.sender.GoogleFormSender;
//import org.acra.sender.ReportSenderException;
//
//import android.app.ActivityManager;
//import android.app.ActivityManager.MemoryInfo;
//import android.content.Context;
//import android.os.Process;
//
//public class AcraSender extends GoogleFormSender {
//    private Context mContext;
//
//    public AcraSender(Context context, String formKey) {
//        super(formKey);
//        mContext = context;
//    }
//
//    @Override
//    public void send(CrashReportData report) throws ReportSenderException {
//        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
//        MemoryInfo outInfo = new MemoryInfo();
//        activityManager.getMemoryInfo(outInfo);
//        String value = "totalMem: " + (getmem_TOLAL() / 1024f) + "\n threshold :" + (outInfo.threshold / 1048576f);
//        int myProcessID = Process.myPid();
//        android.os.Debug.MemoryInfo[] myInfo = activityManager.getProcessMemoryInfo(new int[] { myProcessID });
//        value = value + "\n TotalPrivateDirty :" + (myInfo[0].getTotalPrivateDirty() / 1024f);
//        report.put(ReportField.USER_COMMENT, value);
//
//        int count = SettingsActivity.getAcraReportCount(mContext);
//        long time = SettingsActivity.getACRAReportTime(mContext);
//        long curTime = System.currentTimeMillis();
//        if (time == 0 || Math.abs(curTime - time) > 1000 * 3600 * 24) {
//            SettingsActivity.saveAcraReportTime(mContext, curTime);
//            SettingsActivity.saveAcraReportCount(mContext, 1);
//            super.send(report);
//        } else if (count < 5) {
//            SettingsActivity.saveAcraReportCount(mContext, ++count);
//            super.send(report);
//        }
//
//    }
//
//    private long getmem_TOLAL() {
//        long total;
//        String path = "/proc/meminfo";
//        String content = null;
//        BufferedReader br = null;
//        try {
//            br = new BufferedReader(new FileReader(path), 8);
//            String line;
//            if ((line = br.readLine()) != null) {
//                content = line;
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            if (br != null) {
//                try {
//                    br.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        int begin = content.indexOf(':');
//        int end = content.indexOf('k');
//        content = content.substring(begin + 1, end).trim();
//        total = Integer.parseInt(content);
//        return total;
//    }
//}
