package com.video.utils;

public class Contants {
	
	public static String processFolder = "C:/Users/user/Desktop/video/"; /* 處理擺放資料夾 */
	public static String exeFolder = "C:/Users/user/Downloads/" ; /* 轉檔程式擺放資料夾 */
	
    public static final String ffmpegpath = exeFolder + "ffmpeg/bin/ffmpeg.exe";     /* ffmpeg 程式安裝位置 */
    public static final String mencoderpath = exeFolder + "mplayer/mencoder.exe";     /* mencoder 程式安裝位置 */

    public static final String videofolder = processFolder + "video/temp/" ;  /* 需要被轉換格式的影片位置 */
    public static final String videoRealPath = processFolder +"image/temp/";    /* 需要被截圖的影片位置 */

    public static final String targetfolder = processFolder + "video/"; /* 轉檔後影片儲存位置 */
    public static final String imageRealPath = processFolder + "image/"; /* 截圖存放位置 */
    
	public static final String tempPath = processFolder + "temp/"; /* 上傳的多格式的影片檔-作為暫存路徑儲存，轉檔以後刪除-路徑不能寫// */


}
