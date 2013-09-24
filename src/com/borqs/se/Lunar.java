/*
 * Copyright (C) 2010 The Open Mobile System Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.borqs.se;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lunar {
    private final static int[] lunarInfo = { 0x4bd8, 0x4ae0, 0xa570, 0x54d5, 0xd260, 0xd950, 0x5554, 0x56af, 0x9ad0,
            0x55d2, // 1900-1909
            0x4ae0, 0xa5b6, 0xa4d0, 0xd250, 0xd295, 0xb54f, 0xd6a0, 0xada2, 0x95b0, 0x4977, // 1910-1919
            0x497f, 0xa4b0, 0xb4b5, 0x6a50, 0x6d40, 0xab54, 0x2b6f, 0x9570, 0x52f2, 0x4970, // 1920-1929
            0x6566, 0xd4a0, 0xea50, 0x6a95, 0x5adf, 0x2b60, 0x86e3, 0x92ef, 0xc8d7, 0xc95f, // 1930-1939
            0xd4a0, 0xd8a6, 0xb55f, 0x56a0, 0xa5b4, 0x25df, 0x92d0, 0xd2b2, 0xa950, 0xb557, // 1940-1949
            0x6ca0, 0xb550, 0x5355, 0x4daf, 0xa5b0, 0x4573, 0x52bf, 0xa9a8, 0xe950, 0x6aa0, // 1950-1959
            0xaea6, 0xab50, 0x4b60, 0xaae4, 0xa570, 0x5260, 0xf263, 0xd950, 0x5b57, 0x56a0, // 1960-1969
            0x96d0, 0x4dd5, 0x4ad0, 0xa4d0, 0xd4d4, 0xd250, 0xd558, 0xb540, 0xb6a0, 0x95a6, // 1970-1979
            0x95bf, 0x49b0, 0xa974, 0xa4b0, 0xb27a, 0x6a50, 0x6d40, 0xaf46, 0xab60, 0x9570, // 1980-1989
            0x4af5, 0x4970, 0x64b0, 0x74a3, 0xea50, 0x6b58, 0x5ac0, 0xab60, 0x96d5, 0x92e0, // 1990-1999
            0xc960, 0xd954, 0xd4a0, 0xda50, 0x7552, 0x56a0, 0xabb7, 0x25d0, 0x92d0, 0xcab5, // 2000-2009
            0xa950, 0xb4a0, 0xbaa4, 0xad50, 0x55d9, 0x4ba0, 0xa5b0, 0x5176, 0x52bf, 0xa930, // 2010-2019
            0x7954, 0x6aa0, 0xad50, 0x5b52, 0x4b60, 0xa6e6, 0xa4e0, 0xd260, 0xea65, 0xd530, // 2020-2029
            0x5aa0, 0x76a3, 0x96d0, 0x4afb, 0x4ad0, 0xa4d0, 0xd0b6, 0xd25f, 0xd520, 0xdd45, // 2030-2039
            0xb5a0, 0x56d0, 0x55b2, 0x49b0, 0xa577, 0xa4b0, 0xaa50, 0xb255, 0x6d2f, 0xada0, // 2040-2049
            0x4b63, 0x937f, 0x49f8, 0x4970, 0x64b0, 0x68a6, 0xea5f, 0x6b20, 0xa6c4, 0xaaef, // 2050-2059
            0x92e0, 0xd2e3, 0xc960, 0xd557, 0xd4a0, 0xda50, 0x5d55, 0x56a0, 0xa6d0, 0x55d4, // 2060-2069
            0x52d0, 0xa9b8, 0xa950, 0xb4a0, 0xb6a6, 0xad50, 0x55a0, 0xaba4, 0xa5b0, 0x52b0, // 2070-2079
            0xb273, 0x6930, 0x7337, 0x6aa0, 0xad50, 0x4b55, 0x4b6f, 0xa570, 0x54e4, 0xd260, // 2080-2089
            0xe968, 0xd520, 0xdaa0, 0x6aa6, 0x56df, 0x4ae0, 0xa9d4, 0xa4d0, 0xd150, 0xf252, // 2090-2099
            0xd520 // 2100
    };
    // Solar Term Offset
    private final static int[] solarTermInfo = { 0, 21208, 42467, 63836, 85337, 107014, 128867, 150921, 173149, 195551,
            218072, 240693, 263343, 285989, 308563, 331033, 353350, 375494, 397447, 419210, 440795, 462224, 483532,
            504758 };
    // Heavenly Stems
    public final static String[] Tianan = { "\u7532", "\u4e59", "\u4e19", "\u4e01", "\u620a", "\u5df1", "\u5e9a",
            "\u8f9b", "\u58ec", "\u7678"
    // "甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸"
    };
    // Earthly Branches
    public final static String[] Deqi = { "\u5b50", "\u4e11", "\u5bc5", "\u536f", "\u8fb0", "\u5df3", "\u5348",
            "\u672a", "\u7533", "\u9149", "\u620c", "\u4ea5"
    // "子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥"
    };
    // Zodiac Animals
    public final static String[] Animals = { "\u9f20", "\u725b", "\u864e", "\u5154", "\u9f99", "\u86c7", "\u9a6c",
            "\u7f8a", "\u7334", "\u9e21", "\u72d7", "\u732a"
    // "鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪"
    };
    // Solar Term Strings
    public final static String[] solarTerm = { "\u5c0f\u5bd2", "\u5927\u5bd2", "\u7acb\u6625", "\u96e8\u6c34",
            "\u60ca\u86f0", "\u6625\u5206", "\u6e05\u660e\u8282", "\u8c37\u96e8", "\u7acb\u590f", "\u5c0f\u6ee1",
            "\u8292\u79cd", "\u590f\u81f3", "\u5c0f\u6691", "\u5927\u6691", "\u7acb\u79cb", "\u5904\u6691",
            "\u767d\u9732", "\u79cb\u5206", "\u5bd2\u9732", "\u971c\u964d", "\u7acb\u51ac", "\u5c0f\u96ea",
            "\u5927\u96ea", "\u51ac\u81f3"
    // "小寒", "大寒", "立春", "雨水", "惊蛰", "春分",
    // "清明节", "谷雨", "立夏", "小满", "芒种", "夏至",
    // "小暑", "大暑", "立秋", "处暑", "白露", "秋分",
    // "寒露", "霜降", "立冬", "小雪", "大雪", "冬至"
    };
    public final static String[] lunarString1 = { "\u96f6", "\u4e00", "\u4e8c", "\u4e09", "\u56db", "\u4e94", "\u516d",
            "\u4e03", "\u516b", "\u4e5d"
    // "零", "一", "二", "三", "四", "五", "六", "七", "八", "九"
    };
    public final static String[] lunarString2 = { "\u521d", "\u5341", "\u5eff", "\u4e09", "\u6b63", "\u814a", "\u51ac",
            "\u95f0", "\u6708"
    // "初", "十", "廿", "三", "正", "腊", "冬", "闰", "月"
    };
    /**
     * Solar Festivals, * show holidays.
     */
    private final static String[] sFtv = { "0101*\u5143\u65e6", "0214 \u60c5\u4eba\u8282", "0308 \u5987\u5973\u8282",
            "0501*\u52b3\u52a8\u8282", "0504 \u9752\u5e74\u8282", "0601 \u513f\u7ae5\u8282", "0701 \u5efa\u515a\u8282",
            "0801 \u5efa\u519b\u8282", "0910 \u6559\u5e08\u8282", "1001*\u56fd\u5e86\u8282", "1225 \u5723\u8bde\u8282"
    // "0101*元旦", "0214 情人节", "0308 妇女节", "0501*劳动节",
    // "0504 青年节", "0601 儿童节", "0701 建党节",
    // "0801 建军节", "0910 教师节", "1001*国庆节", "1225 圣诞节"
    };
    /**
     * Lunar Festivals, * show holidays.
     */
    private final static String[] lFtv = { "0101*\u6625\u8282", "0115 \u5143\u5bb5\u8282", "0505*\u7aef\u5348\u8282",
            "0707 \u4e03\u5915", "0815*\u4e2d\u79cb\u8282", "0909 \u91cd\u9633\u8282", "1208 \u814a\u516b\u8282",
            "0100*\u9664\u5915"
    // "0101*春节", "0115 元宵节", "0505*端午节",
    // "0707 七夕", "0815*中秋节", "0909 重阳节",
    // "1208 腊八节","0100*除夕" , "
    };
    /**
     * Calculate Festivals, which week day in which month.
     */
    private static String[] wFtv = {
    // "0520 母亲节" "0520 \u6bcd\u4eb2\u8282"
    };

    private boolean isSolarTerm = false;
    private boolean isFinded = false;
    private boolean isSFestival = false;
    private boolean isLFestival = false;
    private String sFestivalName = "";
    private String lFestivalName = "";
    private String description = "";
    private boolean isHoliday = false;
    private boolean isSHoliday = false;
    private boolean isLHoliday = false;

    private Calendar solar;
    private int lunarYear;
    private int lunarMonth;
    private int lunarDay;
    private boolean isLeap;
    private boolean isLeapYear;
    private int solarYear;
    private int solarMonth;
    private int solarDay;
    private int cyclicalYear = 0;
    private int cyclicalMonth = 0;
    private int cyclicalDay = 0;
    private int maxDayInMonth = 29;

    /**
     * Construct the information of the lunar calendar through Date instance
     * 
     * @param date
     *            Appoint the date target
     */
    public Lunar(Date date) {
        if (date == null)
            date = new Date();
        this.init(date.getTime());
    }

    /**
     * Construct the information of the lunar calendar through TimeInMillis
     * 
     * @param TimeInMillis
     */
    public Lunar(long TimeInMillis) {
        this.init(TimeInMillis);
    }

    private void init(long TimeInMillis) {
        this.solar = Calendar.getInstance();
        this.solar.setTimeInMillis(TimeInMillis);
        this.solarYear = this.solar.get(Calendar.YEAR);
        this.solarMonth = this.solar.get(Calendar.MONTH);
        this.solarDay = this.solar.get(Calendar.DAY_OF_MONTH);

        Calendar baseDate = new GregorianCalendar(1900, 0, 31);
        long offset = (TimeInMillis - baseDate.getTimeInMillis()) / 86400000; // The
                                                                              // days
                                                                              // of
                                                                              // displacement

        // Decrease progressively according to lunar calendar year the total
        // days in each lunar calendar year,
        // confirm the year of lunar calendar.
        this.lunarYear = 1900;
        int daysInLunarYear = Lunar.getLunarYearDays(this.lunarYear);
        while (this.lunarYear < 2100 && offset >= daysInLunarYear) {
            offset -= daysInLunarYear;
            daysInLunarYear = Lunar.getLunarYearDays(++this.lunarYear);
        }

        // Decrease progressively according to lunar calendar month the total
        // days in each lunar calendar month,
        // confirm the year of lunar calendar .
        int lunarMonth = 1;
        int leapMonth = Lunar.getLunarLeapMonth(this.lunarYear);
        this.isLeapYear = leapMonth > 0;// whether it is leap month

        // Whether the leap month decrease progressively.
        boolean leapDec = false;
        boolean isLeap = false;
        int daysInLunarMonth = 0;

        while (lunarMonth < 13 && offset > 0) {
            if (isLeap && leapDec) {
                // It is leap year and leap month
                // the total days of leap month in that lunar calendar year
                daysInLunarMonth = Lunar.getLunarLeapDays(this.lunarYear);
                leapDec = false;
            } else {
                // the total days of the appointed month in that lunar calendar
                // year
                daysInLunarMonth = Lunar.getLunarMonthDays(this.lunarYear, lunarMonth);
            }
            if (offset < daysInLunarMonth) {
                break;
            }
            offset -= daysInLunarMonth;
            if (leapMonth == lunarMonth && isLeap == false) {
                // next month is leap month
                leapDec = true;
                isLeap = true;
            } else {
                lunarMonth++;
            }
        }

        this.maxDayInMonth = daysInLunarMonth;
        this.lunarMonth = lunarMonth;
        this.isLeap = (lunarMonth == leapMonth && isLeap);// whether it is leap
                                                          // month
        // Obtain the lunar calendar date Figure
        this.lunarDay = (int) offset + 1;

        // Obtain the Heavenly Stems and Earthly Branches calendar
        // this.getCyclicalData();
    }

    /**
     * Appoint the year of lunar calendar. Return in month in leap month of this
     * year.
     * 
     * @param lunarYear
     *            Appointed lunar calendar Year (figure)
     * @return Month in leap month of this lunar calendar year (figure, if not
     *         have leap month,then return 0)
     */
    private static int getLunarLeapMonth(int lunarYear) {
        // In the data list, each lunar calendar year is expressed with 16bit ,
        // the former 12bit means the Solar month of 30 days and 29 days in 12
        // months separately, the last 4bit means leap month
        // If 4bit are all 1 or 0, it means that month is not leap month,
        // otherwise the value of 4bit is the month of leap month
        int leapMonth = Lunar.lunarInfo[lunarYear - 1900] & 0xf;
        leapMonth = (leapMonth == 0xf ? 0 : leapMonth);
        return leapMonth;
    }

    /**
     * Get the days number in leap month of this lunar calendar year
     * 
     * @param lunarYear
     *            Appointed lunar calendar year (figure)
     * @return The days number (figure) in leap month of this lunar calendar
     *         year
     */
    private static int getLunarLeapDays(int lunarYear) {
        // If the last 4bit is 1111 in next year, return 30 (solar month of 30
        // days)
        // If the last 4bit is not 1111 in next year, return 29(solar month of
        // 29 days)
        // If that year does not have leap month, return 0
        return Lunar.getLunarLeapMonth(lunarYear) > 0 ? ((Lunar.lunarInfo[lunarYear - 1899] & 0xf) == 0xf ? 30 : 29)
                : 0;
    }

    /**
     * Get the total days in lunar calendar year
     * 
     * @param lunarYear
     *            Appoint Lunar calendar Year (figure)
     * @return The total days (figure) in this lunar calendar year
     */
    private static int getLunarYearDays(int lunarYear) {
        // Calculated by a solar month of 30 days, the lunar calendar year has
        // at least 12 * 29 = 348Days
        int daysInLunarYear = 348;
        // In the data list, each lunar calendar year is expressed with 16bit
        // the former 12bit means the Solar month of 30 days and 29 days in 12
        // months separately, the last 4bit means leap month
        // Accumulate for one day each solar month of 30 days
        for (int i = 0x8000; i > 0x8; i >>= 1) {
            daysInLunarYear += ((Lunar.lunarInfo[lunarYear - 1900] & i) != 0) ? 1 : 0;
        }
        // Add the leap month days
        daysInLunarYear += Lunar.getLunarLeapDays(lunarYear);
        return daysInLunarYear;
    }

    /**
     * Get the total days in normal month of lunar calendar year.
     * 
     * @param lunarYear
     *            Appoint the year of lunar calendar (figure)
     * @param lunarMonth
     *            Appoint the month of lunar calendar (figure)
     * @return the total days of one month in this lunar calendar year(figure,if
     *         it is not leap month, then return 0)
     */
    private static int getLunarMonthDays(int lunarYear, int lunarMonth) {
        // In the data list, each lunar calendar year is expressed with 16bit ,
        // the former 12bit means the Solar month of 30 days and 29 days in 12
        // months separately, the last 4bit means leap month
        int daysInLunarMonth = ((Lunar.lunarInfo[lunarYear - 1900] & (0x10000 >> lunarMonth)) != 0) ? 30 : 29;
        return daysInLunarMonth;
    }

    // Part of Lunar Information
    /**
     * Return Lunar calendar year, month and date string.
     * 
     * @return The lunar calendar string
     */
    public String getLunarDateString() {
        return this.getLunarYearString() + "\u5e74" + " " + getAnimalString() + " " + this.getLunarMonthString()
                + this.getLunarDayString();
    }

    /**
     * Get the lunar calendar year string
     * 
     * @return the lunar calendar year string
     */
    public String getLunarYearString() {
        return Lunar.getLunarYearString(this.lunarYear);
    }

    /**
     * Get the lunar calendar month string
     * 
     * @return the lunar calendar month string
     */
    public String getLunarMonthString() {
        return (this.isLeap() ? "\u95f0" : "") + Lunar.getLunarMonthString(this.lunarMonth);
    }

    /**
     * Get the lunar calendar date string
     * 
     * @return the lunar calendar date string
     */
    public String getLunarDayString() {
        return Lunar.getLunarDayString(this.lunarDay);
    }

    /**
     * Get the appointed figure of lunar calendar year means string
     * 
     * @param lunarYear
     *            lunar calendar year (figure, 0 is the first of the ten
     *            Heavenly Stems)
     * @return the lunar calendar year string
     */
    private static String getLunarYearString(int lunarYear) {
        return Lunar.getCyclicalString(lunarYear - 1900 + 36);
    }

    /**
     * Get the appointed figure of lunar calendar month means string
     * 
     * @param lunarMonth
     *            lunar calendar month (figure)
     * @return lunar calendar month string
     */
    private static String getLunarMonthString(int lunarMonth) {
        String lunarMonthString = "";
        if (lunarMonth == 1) {
            lunarMonthString = Lunar.lunarString2[4];
        } else {
            if (lunarMonth > 9)
                lunarMonthString += Lunar.lunarString2[1];
            if (lunarMonth % 10 > 0)
                lunarMonthString += Lunar.lunarString1[lunarMonth % 10];
        }
        lunarMonthString += Lunar.lunarString2[8];
        return lunarMonthString;
    }

    /**
     * Get the appointed figure of lunar calendar date means string
     * 
     * @param lunarDay
     *            lunar calendar date(figure)
     * @return lunar calendar date string
     */
    private static String getLunarDayString(int lunarDay) {
        if (lunarDay < 1 || lunarDay > 30)
            return "";
        int i1 = lunarDay / 10;
        int i2 = lunarDay % 10;
        String c1 = Lunar.lunarString2[i1];
        String c2 = Lunar.lunarString1[i2];
        if (lunarDay < 11)
            c1 = Lunar.lunarString2[0];
        if (i2 == 0)
            c2 = Lunar.lunarString2[1];
        return c1 + c2;
    }

    /**
     * Get Lunar calendar year
     * 
     * @return Lunar calendar year (figure)
     */
    public int getLunarYear() {
        return lunarYear;
    }

    /**
     * Get Lunar calendar month
     * 
     * @return Lunar calendar month (figure)
     */
    public int getLunarMonth() {
        return lunarMonth;
    }

    /**
     * Get Lunar calendar date
     * 
     * @return Lunar calendar date (figure)
     */
    public int getLunarDay() {
        return lunarDay;
    }

    /**
     * Get Solar calendar year
     * 
     * @return Solar calendar year (figure)
     */
    public int getSolarYear() {
        return solarYear;
    }

    /**
     * Get Solar calendar month
     * 
     * @return Solar calendar month (figure not calculated from 0)
     */
    public int getSolarMonth() {
        return solarMonth + 1;
    }

    /**
     * Get Solar calendar date
     * 
     * @return Solar calendar date (figure)
     */
    public int getSolarDay() {
        return solarDay;
    }

    /**
     * How many days there is the lunar calendar month at present
     * 
     * @return the days number of the lunar calendar month at present
     */
    public int getMaxDayInMonth() {
        return this.maxDayInMonth;
    }

    /**
     * Fetch the year of birth for lunar calendar year
     * 
     * @return The year of birth in lunar calendar year (example: Dragon)
     */
    public String getAnimalString() {
        return Lunar.Animals[(this.lunarYear - 4) % 12];
    }

    /**
     * On what week day is it
     * 
     * @return week day (Sunday is 1, Saturday is 7)
     */
    public int getDayOfWeek() {
        return this.solar.get(Calendar.DAY_OF_WEEK);
    }

    /**
     * whether the lunar calendar month is leap month
     * 
     * @return whether the lunar calendar month is leap month(true or false)
     */
    public boolean isLeap() {
        return isLeap;
    }

    /**
     * whether the lunar calendar year is leap year
     * 
     * @return whether the lunar calendar year is leap year(true or false)
     */
    public boolean isLeapYear() {
        return isLeapYear;
    }

    /**
     * Is the lunar calendar month 3o days at present
     * 
     * @return Is the lunar calendar month 3o days at present (true or false)
     */
    public boolean isBigMonth() {
        return this.getMaxDayInMonth() > 29;
    }

    /**
     * whether it is today
     * 
     * @return whether it is today(true or false)
     */
    public boolean isToday() {
        Calendar clr = Calendar.getInstance();
        return clr.get(Calendar.YEAR) == this.solarYear && clr.get(Calendar.MONTH) == this.solarMonth
                && clr.get(Calendar.DAY_OF_MONTH) == this.solarDay;
    }

    /**
     * Black Friday
     * 
     * @return whether it is Black Friday (true or false)
     */
    public boolean isBlackFriday() {
        return (this.getSolarDay() == 13 && this.solar.get(Calendar.DAY_OF_WEEK) == 6);
    }

    // Part of Solar Term Information
    /**
     * whether this day is solar term
     * 
     * @return whether this day is solar term(true or false)
     */
    public boolean isSolarTerm() {
        this.getTermString();
        return isSolarTerm;
    }

    /**
     * Get the solar term string of the solar calendar date
     * 
     * @return The twenty-four solar terms string, if it is not the day of solar
     *         terms, return empty string
     */
    public String getTermString() {
        String termString = "";
        if (Lunar.getSolarTermDay(solarYear, solarMonth * 2) == solarDay) {
            termString = Lunar.solarTerm[solarMonth * 2];
            isSolarTerm = true;
        } else if (Lunar.getSolarTermDay(solarYear, solarMonth * 2 + 1) == solarDay) {
            termString = Lunar.solarTerm[solarMonth * 2 + 1];
            isSolarTerm = true;
        }
        return termString;
    }

    private static GregorianCalendar utcCal = null;

    private static synchronized void makeUTCCalendar() {
        if (Lunar.utcCal == null) {
            Lunar.utcCal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        }
    }

    /**
     * Get date value expressed with the Universal Time Coordinated (UTC)
     * through date instance
     * 
     * @param Appointed
     *            date instance
     * @return date value expressed with the Universal Time Coordinated (UTC)
     */
    public static synchronized int getUTCDay(Date date) {
        Lunar.makeUTCCalendar();
        synchronized (utcCal) {
            utcCal.clear();
            utcCal.setTimeInMillis(date.getTime());
            return utcCal.get(Calendar.DAY_OF_MONTH);
        }
    }

    /**
     * Get the millisecond between January 1, 1970 in the Universal Time
     * Coordinated (UTC or GMT) and the appointed dates
     * 
     * @param y
     *            Appointed year
     * @param m
     *            Appointed month
     * @param d
     *            Appointed date
     * @param h
     *            Appointed hour
     * @param min
     *            Appointed minute
     * @param sec
     *            Appointed second
     * @return the millisecond between January 1, 1970 in the Universal Time
     *         Coordinated (UTC or GMT) and the appointed dates
     */
    public static synchronized long UTC(int y, int m, int d, int h, int min, int sec) {
        Lunar.makeUTCCalendar();
        synchronized (utcCal) {
            utcCal.clear();
            utcCal.set(y, m, d, h, min, sec);
            return utcCal.getTimeInMillis();
        }
    }

    /**
     * Get the date of appointed solar term in this solar calendar year
     * 
     * @param solarYear
     *            Appoint the Solar Calendar year (figure)
     * @param index
     *            Appoint the solar term index (figure, 0 from Slight Cold)
     * @return Date (figure, the day of that month)
     */
    private static int getSolarTermDay(int solarYear, int index) {
        long millis = (long) 31556925974.7 * (solarYear - 1900) + solarTermInfo[index] * 60000L;
        millis = millis + Lunar.UTC(1900, 0, 6, 2, 5, 0);
        return Lunar.getUTCDay(new Date(millis));
    }

    // Part of Solar and Lunar Festivals Information
    private final static Pattern sFreg = Pattern.compile("^(\\d{2})(\\d{2})([\\s\\*])(.+)$");
    private final static Pattern wFreg = Pattern.compile("^(\\d{2})(\\d)(\\d)([\\s\\*])(.+)$");

    private synchronized void findFestival() {
        int sM = this.getSolarMonth();
        int sD = this.getSolarDay();
        int lM = this.getLunarMonth();
        int lD = this.getLunarDay();
        // int sy = this.getSolarYear();

        Matcher m;
        // Solar Festivals
        for (int i = 0; i < Lunar.sFtv.length; i++) {
            m = Lunar.sFreg.matcher(Lunar.sFtv[i]);
            if (m.find()) {
                if (sM == Lunar.toInt(m.group(1)) && sD == Lunar.toInt(m.group(2))) {
                    this.isSFestival = true;
                    this.sFestivalName = m.group(4);
                    if ("*".equals(m.group(3))) {
                        this.isHoliday = true; // This day is a holiday.
                        this.isSHoliday = true; // And it is a solar holiday.
                    }
                    break;
                }
            }
        }
        // Lunar Festivals
        for (int i = 0; i < Lunar.lFtv.length; i++) {
            m = Lunar.sFreg.matcher(Lunar.lFtv[i]);
            if (m.find()) {
                if (lM == Lunar.toInt(m.group(1)) && lD == Lunar.toInt(m.group(2))) {
                    this.isLFestival = true;
                    this.lFestivalName = m.group(4);
                    if ("*".equals(m.group(3))) {
                        this.isHoliday = true; // This day is a holiday.
                        this.isLHoliday = true; // And it is a lunar holiday.
                    }
                    break;
                }
            }
        }

        // Calculate Festivals, which day in which week
        int w, d;
        for (int i = 0; i < Lunar.wFtv.length; i++) {
            m = Lunar.wFreg.matcher(Lunar.wFtv[i]);
            if (m.find()) {
                if (this.getSolarMonth() == Lunar.toInt(m.group(1))) {
                    w = Lunar.toInt(m.group(2));// week
                    d = Lunar.toInt(m.group(3));// day
                    if (this.solar.get(Calendar.WEEK_OF_MONTH) == w && this.solar.get(Calendar.DAY_OF_WEEK) == d) {
                        this.isSFestival = true;
                        this.sFestivalName += "|" + m.group(5);
                        if ("*".equals(m.group(4))) {
                            this.isHoliday = true;
                        }
                    }
                }
            }
        }

        // Date Description
        // if(sy>1874 && sy<1909) this.description = "光绪" +
        // (((sy-1874)==1)?"元":""+(sy-1874));
        // if(sy>1908 && sy<1912) this.description = "宣统" +
        // (((sy-1908)==1)?"元":String.valueOf(sy-1908));
        // if(sy>1911 && sy<1950) this.description = "民国" +
        // (((sy-1911)==1)?"元":String.valueOf(sy-1911));
        // if(sy>1949) this.description = "共和国" +
        // (((sy-1949)==1)?"元":String.valueOf(sy-1949));
        // this.description += "年";
        this.sFestivalName = this.sFestivalName.replaceFirst("^\\|", "");
        this.isFinded = true;
    }

    private static int toInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Get the Solar Calendar Festival Name
     * 
     * @return the solar calendar festival name string, if it is not festival,
     *         then return empty string
     */
    public String getSFestivalName() {
        return this.sFestivalName;
    }

    /**
     * Get the Lunar Calendar Festival Name
     * 
     * @return the lunar calendar festival name string, if it is not festival,
     *         then return empty string
     */
    public String getLFestivalName() {
        return this.lFestivalName;
    }

    /**
     * Whether it is the Solar Festival
     * 
     * @return Whether it is the Solar Festival(true or false)
     */
    public boolean isSFestival() {
        if (!this.isFinded)
            this.findFestival();
        return this.isSFestival;
    }

    /**
     * Whether it is the Lunar Festival
     * 
     * @return Whether it is the Lunar Festival(true or false)
     */
    public boolean isLFestival() {
        if (!this.isFinded)
            this.findFestival();
        return this.isLFestival;
    }

    /**
     * Whether it is festival
     * 
     * @return Whether it is festival(true or false)
     */
    public boolean isFestival() {
        return this.isSFestival() || this.isLFestival();
    }

    /**
     * Whether it is holiday
     * 
     * @return Whether it is holiday (true or false)
     */
    public boolean isHoliday() {
        if (!this.isFinded)
            this.findFestival();
        return this.isHoliday;
    }

    /**
     * Whether it is the Solar holiday
     * 
     * @return Whether it is the Solar holiday (true or false)
     */
    public boolean isSHoliday() {
        if (!this.isFinded)
            this.findFestival();
        return this.isSHoliday;
    }

    /**
     * Whether it is the Lunar holiday
     * 
     * @return Whether it is the Lunar holiday (true or false)
     */
    public boolean isLHoliday() {
        if (!this.isFinded)
            this.findFestival();
        return this.isLHoliday;
    }

    /**
     * Get the date description
     * 
     * @return the date description string
     */
    // public String getDescription() {
    // if (!this.isFinded) this.findFestival();
    // return this.description;
    // }

    // Part of the Heavenly Stems and Earthly Branches Calendar Information
    /**
     * Initialize the Heavenly Stems and Earthly Branches(HSEB) Calendar through
     * this lunar instance
     */
    public void getCyclicalData() {
        // this.solarYear = this.solar.get(Calendar.YEAR);
        // this.solarMonth = this.solar.get(Calendar.MONTH);
        // this.solarDay = this.solar.get(Calendar.DAY_OF_MONTH);
        int cyclicalYear = 0;
        int cyclicalMonth = 0;
        int cyclicalDay = 0;

        // Initialize the Heavenly Stems and Earthly Branches calendar year
        // After the Beginning of spring in 1900, it is Geng Zi year(36 for 60
        // hex)
        int term2 = Lunar.getSolarTermDay(solarYear, 2); // the date of the
                                                         // Beginning of spring
        // Adjust the annual column divided in February in accordance with solar
        // terms, make the Beginning of spring as the boundary
        if (solarMonth < 1 || (solarMonth == 1 && solarDay < term2)) {
            cyclicalYear = (solarYear - 1900 + 36 - 1) % 60;
        } else {
            cyclicalYear = (solarYear - 1900 + 36) % 60;
        }

        // Initialize the Heavenly Stems and Earthly Branches calendar month
        // Before the Slight Cold in January, 1900, it is Bing Zi month (12 for
        // 60 hex)
        int firstNode = Lunar.getSolarTermDay(solarYear, solarMonth * 2); // the
                                                                          // date
                                                                          // of
                                                                          // the
                                                                          // first
                                                                          // solar
                                                                          // term
                                                                          // in
                                                                          // the
                                                                          // current
                                                                          // month
        // Depend on the monthly column of solar terms, make terms as the
        // boundary
        if (solarDay < firstNode) {
            cyclicalMonth = ((solarYear - 1900) * 12 + solarMonth + 12) % 60;
        } else {
            cyclicalMonth = ((solarYear - 1900) * 12 + solarMonth + 13) % 60;
        }

        // Initialize the Heavenly Stems and Earthly Branches calendar date
        // Calculate the days between the first day in the current month and
        // 1900,1,1
        // the days between 1900,1,1 and 1970,1,1 is 25567 days
        // the day column in 1900,1,1 is Jia Xu date (10 for 60 hex)
        cyclicalDay = (int) (Lunar.UTC(solarYear, solarMonth, solarDay, 0, 0, 0) / 86400000 + 25567 + 10) % 60;

        this.cyclicalYear = cyclicalYear;
        this.cyclicalMonth = cyclicalMonth;
        this.cyclicalDay = cyclicalDay;
    }

    /**
     * Get the Heavenly Stems and Earthly Branches calendar string
     * 
     * @return The Heavenly Stems and Earthly Branches string (example:
     *         甲子年甲子月甲子日)
     */
    public String getCyclicalDateString() {
        return this.getCyclicaYear() + "\u5e74" + this.getCyclicaMonth() + "\u6708" + this.getCyclicaDay() + "\u65e5";
    }

    /**
     * Get the Heavenly Stems and Earthly Branches calendar year string
     * 
     * @return the Heavenly Stems and Earthly Branches calendar year string
     */
    public String getCyclicaYear() {
        return Lunar.getCyclicalString(this.cyclicalYear);
    }

    /**
     * Get the Heavenly Stems and Earthly Branches calendar month string
     * 
     * @return the Heavenly Stems and Earthly Branches calendar month string
     */
    public String getCyclicaMonth() {
        return Lunar.getCyclicalString(this.cyclicalMonth);
    }

    /**
     * Get the Heavenly Stems and Earthly Branches calendar date string
     * 
     * @return the Heavenly Stems and Earthly Branches calendar date string
     */
    public String getCyclicaDay() {
        return Lunar.getCyclicalString(this.cyclicalDay);
    }

    /**
     * Get the Heavenly Stems year
     * 
     * @return the Heavenly Stems year (figure)
     */
    public int getTiananY() {
        return Lunar.getTianan(this.cyclicalYear);
    }

    /**
     * Get the Heavenly Stems month
     * 
     * @return Get the Heavenly Stems month(figure)
     */
    public int getTiananM() {
        return Lunar.getTianan(this.cyclicalMonth);
    }

    /**
     * Get the Heavenly Stems date
     * 
     * @return the Heavenly Stems date (figure)
     */
    public int getTiananD() {
        return Lunar.getTianan(this.cyclicalDay);
    }

    /**
     * Get the Earthly Branches year
     * 
     * @return the Earthly Branches year (figure)
     */
    public int getDeqiY() {
        return Lunar.getDeqi(this.cyclicalYear);
    }

    /**
     * Get the Earthly Branches month
     * 
     * @return the Earthly Branches month (figure)
     */
    public int getDeqiM() {
        return Lunar.getDeqi(this.cyclicalMonth);
    }

    /**
     * Get the Earthly Branches date
     * 
     * @return the Earthly Branches date (figure)
     */
    public int getDeqiD() {
        return Lunar.getDeqi(this.cyclicalDay);
    }

    /**
     * Get the Heavenly Stems and Earthly Branches String
     * 
     * @param cyclicalNumber
     *            Appointed the Heavenly Stems and Earthly Branches position
     *            (figure, 0 is '甲子')
     * @return the Heavenly Stems and Earthly Branches String
     */
    private static String getCyclicalString(int cyclicalNumber) {
        return Lunar.Tianan[Lunar.getTianan(cyclicalNumber)] + Lunar.Deqi[Lunar.getDeqi(cyclicalNumber)];
    }

    /**
     * Get the Heavenly Stems index
     * 
     * @param cyclicalNumber
     *            Appointed the Heavenly Stems and Earthly Branches position
     *            (figure, 0 is '甲子')
     * @return the Heavenly Stems (figure)
     */
    private static int getTianan(int cyclicalNumber) {
        return cyclicalNumber % 10;
    }

    /**
     * Get the Earthly Branches index
     * 
     * @param cyclicalNumber
     *            Appointed the Heavenly Stems and Earthly Branches position
     *            (figure, 0 is '甲子')
     * @return the Earthly Branches (figure)
     */
    private static int getDeqi(int cyclicalNumber) {
        return cyclicalNumber % 12;
    }

}
