package com.video.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.video.exception.ConvertedException;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;

public class ConverVideoUtils {

	private String sourceVideoPath; /* 來源影片路徑 */
	private String filerealname; /* 影片檔名(不包括副檔名) */
	private String filename; /* 影片檔名(包括副檔名) */
	private String videofolder = Contants.videofolder; /* 其餘格式的影片路徑 */
	private String targetfolder = Contants.targetfolder; /* 影片路徑 */
	private String ffmpegpath = Contants.ffmpegpath; /* ffmpeg.exe的路徑 */
	private String mencoderpath = Contants.mencoderpath; /* mencoder的路徑 */
	private String imageRealPath = Contants.imageRealPath; /* 截圖存放路徑 */

	public ConverVideoUtils() {
	}

	/* 轉檔的方法，傳入原始影片 */
	public ConverVideoUtils(String path) {
		sourceVideoPath = path;
	}

	/* Set與Get 傳遞path */
	public String getPATH() {
		return sourceVideoPath;
	}

	public void setPATH(String path) {
		sourceVideoPath = path;
	}

	/**
	 * 轉換影片格式
	 * 
	 * @param String
	 *            targetExtension 目標影片副檔名 .xxx
	 * @param boolean
	 *            isDelSourseFile 轉檔完成後是否刪除原始檔案
	 * @param String
	 *            resolution 轉檔影片解析度
	 * @return
	 * @throws Exception
	 */
	public boolean beginConver(String targetExtension, String resolution) throws Exception {
		File fi = new File(sourceVideoPath);

		filename = fi.getName(); /* 取得影片名稱(包括副檔名) */

		filerealname = filename.substring(0, filename
				.lastIndexOf(".")); /* 取得影片名稱(不包括副檔名)- 後面加 .toLowerCase()小寫 */

		System.out.println("----接收到檔案(" + sourceVideoPath + ")需要轉換-------");

		/* 檢查檔案是否存在 */
		/*
		 * if (checkfile(sourceVideoPath)) { System.out.println(sourceVideoPath
		 * + "========此檔案存在 "); return false; }
		 */

		System.out.println("----開始轉檔(" + sourceVideoPath + ")-------------------------- ");

		/* 進行轉檔 */
		if (process(targetExtension, resolution)) {

			System.out.println("影片轉檔結束，開始截圖================= ");

			/* 影片轉檔完成，使用截圖功能 */
			if (processImg(sourceVideoPath)) {
				System.out.println("截圖成功！ ");
			} else {
				System.out.println("截圖失敗！ ");
			}

			// 刪除原始影片 + 暫存影片
			/*
			 * if (isDelSourseFile) { deleteFile(sourceVideoPath); }
			 */

			/*
			 * File file1 = new File(sourceVideoPath); if (file1.exists()){
			 * System.out.println("刪除原始檔案-可用："+sourceVideoPath); file1.delete();
			 * }
			 */

			String temppath = videofolder + filerealname + ".avi";
			File file2 = new File(temppath);
			if (file2.exists()) {
				System.out.println("刪除暫存影片：" + temppath);
				file2.delete();
			}

			sourceVideoPath = null;
			return true;
		} else {
			sourceVideoPath = null;
			return false;
		}
	}

	/**
	 * 檢查檔案是否存在 - 多處都有判斷
	 * 
	 * @param path
	 * @return
	 */

	/*
	 * private boolean checkfile(String path) { path = sourceVideoPath; File
	 * file = new File(path); try { if (file.exists()) {
	 * System.out.println("影片不存在============="+path); return true; } else {
	 * System.out.println("影片存在"+path); return false; } } catch (Exception e) {
	 * // TODO: handle exception System.out.println("拒絕對影片進行讀取"); } return
	 * false; }
	 */

	/**
	 * 影片截圖功能
	 * 
	 * @param sourceVideoPath
	 *            需要被截圖的影片路徑（包含檔名和副檔名）
	 * @return
	 */
	public boolean processImg(String sourceVideoPath) {

		/* 先確保保存截圖的資料夾存在 */
		File TempFile = new File(imageRealPath);
		if (TempFile.exists()) {
			if (TempFile.isDirectory()) {
				System.out.println("資料夾存在。");
			} else {
				System.out.println("同名的檔案存在，無法建立資料夾。");
			}
		} else {
			System.out.println("資料夾不存在，創建此資料夾。");
			TempFile.mkdirs();
		}

		File fi = new File(sourceVideoPath);
		filename = fi.getName(); /* 取得影片檔名。 */
		filerealname = filename.substring(0,
				filename.lastIndexOf(".")); /* 取得影片檔名(不包括副檔名) */
		// 後面加.toLowerCase()轉為小寫

		List<String> commend = new ArrayList<String>();
		/* 第一幀： 00:00:01 */
		/* 截圖命令：time ffmpeg -ss 00:00:01 -i test1.flv -f image2 -y test1.jpg */

		commend.add(ffmpegpath); /* ffmpeg 程式安裝位置 */
		commend.add("-ss");
		commend.add("00:00:20"); /* 1是代表第1秒的時候截圖 因1幾乎都是一片黑 所以我先改20 */
		commend.add("-i");
		commend.add(sourceVideoPath); /* 截圖的影片位置 */
		commend.add("-f");
		commend.add("image2");
		commend.add("-y");
		commend.add(imageRealPath + filerealname + ".jpg"); // 生成截圖xxx.jpg

		/* 列印截圖指令 */
		StringBuffer test = new StringBuffer();
		for (int i = 0; i < commend.size(); i++) {
			test.append(commend.get(i) + " ");
		}
		System.out.println("截圖指令:" + test);

		/* 轉檔後完成截圖功能-還是得用執行緒來解決 */
		try {
			/*
			 * ProcessBuilder builder = new ProcessBuilder();
			 * builder.command(commend); Process p =builder.start();
			 */
			/* 使用執行緒處理指令 */
			ProcessBuilder builder = new ProcessBuilder();
			builder.command(commend);
			Process p = builder.start();

			/* 取得執行緒的標準輸入流 */
			final InputStream is1 = p.getInputStream();
			/* 取得執行緒的錯誤流 */
			final InputStream is2 = p.getErrorStream();
			/* 啟用兩個執行緒，一個執行緒負責讀標準輸出流，另一個負責堵標準錯誤流 */
			new Thread() {
				public void run() {
					BufferedReader br = new BufferedReader(new InputStreamReader(is1));
					try {
						String lineB = null;
						while ((lineB = br.readLine()) != null) {
							if (lineB != null) {
								// System.out.println(lineB); //必須取走執行緒訊息避免堵塞
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					/* 關閉流 */
					finally {
						try {
							is1.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}

				}
			}.start();
			new Thread() {
				public void run() {
					BufferedReader br2 = new BufferedReader(new InputStreamReader(is2));
					try {
						String lineC = null;
						while ((lineC = br2.readLine()) != null) {
							if (lineC != null) {
								// System.out.println(lineC); //必須取走執行緒訊息避免堵塞
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					}

					/* 關閉流 */
					finally {
						try {
							is2.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}

				}
			}.start();
			/* 等Mencoder進度轉換結束，再使用ffmepg進程 非常重要！！！ */
			p.waitFor();
			System.out.println("截圖進度結束");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 實際轉換影片格式的方法
	 * 
	 * @param targetExtension
	 *            目標影片副檔名
	 * @param isDelSourseFile
	 *            轉檔完成後是否刪除原始檔
	 * @return
	 * @throws Exception
	 */
	private boolean process(String targetExtension, String resolution) throws Exception {

		/* 先判斷影片的類型 - 回傳狀態碼 */
		int type = checkContentType();
		boolean status = false;

		/* 根據狀態碼處理 */
		if (type == 0) {
			System.out.println("ffmpeg可進行轉檔，統一轉為mp4檔");

			status = processVideoFormat(sourceVideoPath, targetExtension, resolution);/* 可以指定轉為什麼格式的影片 */

		} else if (type == 1) {
			/* 如果type為1，將其他檔案先轉換為avi，然後用ffmpeg轉換為指定格式 */
			System.out.println("ffmpeg無法轉檔，先使用mencoder轉檔成avi");
			String avifilepath = processAVI(type);

			if (avifilepath == null) {
				/* 轉檔失敗 avi檔沒有取得 */
				System.out.println("mencoder轉檔失敗，未建立AVI檔案");
				return false;
			} else {
				System.out.println("建立AVI檔案成功，ffmpeg開始轉檔:");
				status = processVideoFormat(avifilepath, targetExtension, resolution);
			}
		}
		return status; /* 執行完成後返回true */
	}

	/**
	 * 檢查檔案類型
	 * 
	 * @return
	 */
	private int checkContentType() {

		/* 取得影片副檔名 */
		String type = sourceVideoPath.substring(sourceVideoPath.lastIndexOf(".") + 1, sourceVideoPath.length())
				.toLowerCase();
		System.out.println("原始檔影片類型為：" + type);

		/* 如果是ffmpeg能解析的格式:(asx，asf，mpg，wmv，3gp，mp4，mov，avi，flv等) */
		if (type.equals("avi")) {
			return 0;
		} else if (type.equals("mpg")) {
			return 0;
		} else if (type.equals("wmv")) {
			return 0;
		} else if (type.equals("3gp")) {
			return 0;
		} else if (type.equals("mov")) {
			return 0;
		} else if (type.equals("mp4")) {
			return 0;
		} else if (type.equals("asf")) {
			return 0;
		} else if (type.equals("asx")) {
			return 0;
		} else if (type.equals("flv")) {
			return 0;
		} else if (type.equals("mkv")) {
			return 0;
		}

		/*
		 * 如果是ffmpeg無法解析的檔案格式(wmv9，rm，rmvb等),
		 * 就先用別的工具（mencoder）轉檔為avi(ffmpeg能解析的)格式.
		 */
		else if (type.equals("wmv9")) {
			return 1;
		} else if (type.equals("rm")) {
			return 1;
		} else if (type.equals("rmvb")) {
			return 1;
		}
		System.out.println("上傳影片格式異常");
		return 9;
	}

	/**
	 * 對ffmpeg無法解析的文件格式(wmv9，rm，rmvb等),
	 * 可以先用（mencoder）轉檔為avi(ffmpeg能解析的)格式.再用ffmpeg解析為指定格式
	 * 
	 * @param type
	 * @return
	 */
	private String processAVI(int type) {

		System.out.println("使用了mencoder.exe程式");
		List<String> commend = new ArrayList<String>();

		commend.add(mencoderpath); /* 指定mencoder.exe程式的位置 */
		commend.add(sourceVideoPath); /* 指定原始影片的位置 */
		commend.add("-oac");
		commend.add("mp3lame"); /* lavc 原mp3lame */
		commend.add("-lameopts");
		commend.add("preset=64");
		commend.add("-ovc");
		commend.add("xvid"); /*
								 * mpg4(xvid),AVC(h.264/x264),
								 * 只有h264才是公認的MP4標準編碼， 如果ck無法播放，就來調整這裡
								 */
		commend.add("-xvidencopts"); /* xvidencopts 或 x264encopts */
		commend.add("bitrate=600"); /* 600或440 */
		commend.add("-of");
		commend.add("avi");
		commend.add("-o");

		commend.add(videofolder + filerealname + ".avi"); /* 存放路徑+名稱，產生.avi影片 */

		/* 列印出轉檔指令 */
		StringBuffer test = new StringBuffer();
		for (int i = 0; i < commend.size(); i++) {
			test.append(commend.get(i) + " ");
		}
		System.out.println("mencoder輸入的指令:" + test);
		// cmd指令：mencoder 1.rmvb -oac mp3lame -lameopts preset=64 -ovc xvid
		// -xvidencopts bitrate=600 -of avi -o rmvb.avi

		try {
			/* 使用執行緒指令啟動轉檔 */
			ProcessBuilder builder = new ProcessBuilder();
			builder.command(commend);
			Process p = builder.start(); /* 多執行緒處理加快速度-解決數據遺失 */
			// doWaitFor(p);

			/* 取得執行緒的標準输入流 */
			final InputStream is1 = p.getInputStream();
			/* 取得執行緒的錯誤流 */
			final InputStream is2 = p.getErrorStream();
			/* 啟動兩個執行緒，一個執行緒負責讀標準輸出流，另一個負責讀標準錯誤流 */
			new Thread() {
				public void run() {
					BufferedReader br = new BufferedReader(new InputStreamReader(is1));
					try {
						String lineB = null;
						while ((lineB = br.readLine()) != null) {
							if (lineB != null) {
								System.out.println(
										lineB); /* 列印mencoder轉檔過程代碼-可註解 */
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					}

					/* 關閉流 */
					/*
					 * finally{ try { is1.close(); } catch (IOException e) {
					 * e.printStackTrace(); } }
					 */

				}
			}.start();
			new Thread() {
				public void run() {
					BufferedReader br2 = new BufferedReader(new InputStreamReader(is2));
					try {
						String lineC = null;
						while ((lineC = br2.readLine()) != null) {
							if (lineC != null) {
								System.out.println(lineC); /* 列印mencoder轉換過程代碼 */
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					}

					/* 關閉 */
					/*
					 * finally{ try { is2.close(); } catch (IOException e) {
					 * e.printStackTrace(); } }
					 */

				}
			}.start();

			/* 等Mencoder進度轉換結束，再調用ffmepg進度非常重要！！！ */
			p.waitFor();
			System.out.println("Mencoder進度结束");
			return videofolder + filerealname + ".avi"; /* 返回轉檔為AVI之後的影片位置 */

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 轉檔為指定格式 ffmpeg能解析的格式：（asx，asf，mpg，wmv，3gp，mp4，mov，avi，flv等）
	 * 
	 * @param oldfilepath
	 * @param targetExtension
	 *            目標格式副檔名 .xxx
	 * @param isDelSourseFile
	 *            轉檔完成後是否刪除原始檔案
	 * @return
	 * @throws Exception
	 */
	private boolean processVideoFormat(String oldfilepath, String targetExtension, String resolution) throws Exception {

		System.out.println("使用了ffmpeg.exe程式");

		/* 先確保保存轉檔後的影片的資料夾存在 */
		File TempFile = new File(targetfolder + resolution + "/");
		if (TempFile.exists()) {
			if (TempFile.isDirectory()) {
				System.out.println("該資料夾存在。");
			} else {
				System.out.println("同名的檔案存在，不能建立資料夾。");
			}
		} else {
			System.out.println("資料夾不存在，建立該資料夾。");
			TempFile.mkdirs();
		}

		List<String> commend = new ArrayList<String>();

		commend.add(ffmpegpath); /* ffmpeg.exe 程式位置 */
		commend.add("-i");
		commend.add(oldfilepath); /* 原始影片位置 */

		commend.add("-vcodec");
		commend.add("h263");
		commend.add("-ab"); /* 新增4條 */
		commend.add("128"); /* 高品質:128 低品質:64 */
		commend.add("-acodec");
		commend.add("mp3"); /* 音訊編碼器：原libmp3lame */
		commend.add("-ac");
		commend.add("2"); /* 原1 */
		commend.add("-ar");
		commend.add("22050"); /* 音訊採樣率22.05kHz */
		commend.add("-r");
		commend.add("29.97"); /* 高品質:29.97 低品質:15 */
		commend.add("-c:v");
		commend.add("libx264"); /* 影片編碼器：影片是h.264編碼格式 */
		commend.add("-strict");
		commend.add("-2");
		commend.add("-s");
		commend.add(resolution); /* 影片解析度 */
		commend.add(targetfolder + resolution + "/" + filerealname
				+ targetExtension); /* 轉檔後的位置+名稱，是指定副檔名的影片 */

		/* 列印命令 */
		StringBuffer test = new StringBuffer();
		for (int i = 0; i < commend.size(); i++) {
			test.append(commend.get(i) + " ");
		}
		System.out.println("ffmpeg輸入的命令:" + test);

		try {
			/* 多執行緒處理加快速度-解決rmvb數據遺失builder名稱要相同 */
			ProcessBuilder builder = new ProcessBuilder();
			builder.command(commend);
			Process p = builder.start(); /* 多執行緒處理加快速度-解決數據遺失 */

			final InputStream is1 = p.getInputStream();
			final InputStream is2 = p.getErrorStream();
			new Thread() {
				public void run() {
					BufferedReader br = new BufferedReader(new InputStreamReader(is1));
					try {
						String lineB = null;
						while ((lineB = br.readLine()) != null) {
							if (lineB != null)
								System.out.println(lineB); /* 列印mencoder轉檔過程代碼 */
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}.start();
			new Thread() {
				public void run() {
					BufferedReader br2 = new BufferedReader(new InputStreamReader(is2));
					try {
						String lineC = null;
						while ((lineC = br2.readLine()) != null) {
							if (lineC != null)
								System.out.println(lineC); /* 列印mencoder轉檔過程代碼 */
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}.start();
			p.waitFor(); /* 進度等待機制，必須要有，否則無法產生mp4！！！ */
			System.gc(); /* 將垃圾清除，暫存檔才能刪除 */
			File createFile = new File(targetfolder + resolution + "/" + filerealname + ".mp4"); /* 檢查檔案是否有被轉成功 */
			if (createFile.length() == 0) {
				createFile.delete(); /* 轉檔失敗 移除檔案 */
				throw new ConvertedException();
			}
		} catch (ConvertedException e) {
			throw new ConvertedException();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}
}
