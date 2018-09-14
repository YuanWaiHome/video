package com.video.upload;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.video.exception.ConvertedException;
import com.video.utils.ConverVideoUtils;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;

public class ConverVideoTest {

	/* Local測試專用 */
	public static void main(String[] args) {
		// ConverVideoTest c = new ConverVideoTest();
		// String yuanPATH = "C:/Users/user/Desktop/video/1.wmv"; /* local影片 */
		// c.run(yuanPATH);
	}

	/* web調用 */
	public void run(String yuanPATH) throws Exception {
		try {
			/* 轉碼與截圖功能 */

			// String filePath = "D:/testfile/video/rmTest.rm"; //Local影片測試

			List<String> resolutionList = getResolution(yuanPATH);
			/* 開始轉檔 */
			int i = 0;
			for (String resolution : resolutionList) {
				String filePath = yuanPATH; /* web端傳入的影片 */
				System.out.println("ConverVideoTest 回傳傳入的影片為:" + filePath);
				ConverVideoUtils zout = new ConverVideoUtils(filePath); /* 傳入path */
				String targetExtension = ".mp4"; /* 設定轉換的格式 */
				zout.beginConver(targetExtension, resolution);
				System.out.println("========================解析度:【  " + resolution + " 】已轉檔完成========================");
			}
			// boolean beginConver = zout.beginConver(targetExtension,
			// isDelSourseFile);
			// System.out.println(beginConver);

		} catch (ConvertedException e) {
			throw new ConvertedException();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 取得影片分辨率
	 * 
	 * @param filePath
	 * @return
	 */
	public List<String> getResolution(String filePath) {
		List<String> resolution = new ArrayList<>();
		IContainer container = IContainer.make();

		// Open up the container
		if (container.open(filePath, IContainer.Type.READ, null) < 0)
			throw new IllegalArgumentException("無法讀取檔案，發生IllegalArgumentException");

		// query how many streams the call to open found
		int numStreams = container.getNumStreams();

		// and iterate through the streams to find the first video stream
		int videoStreamId = -1;
		IStreamCoder videoCoder = null;
		for (int i = 0; i < numStreams; i++) {
			// Find the stream object
			IStream stream = container.getStream(i);
			// Get the pre-configured decoder that can decode this stream;
			IStreamCoder coder = stream.getStreamCoder();

			if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO) {
				videoStreamId = i;
				videoCoder = coder;
				break;
			}
		}
		if (videoStreamId == -1)
			throw new RuntimeException("無法在檔案中找到影片流，發生RuntimeException");

		/*
		 * Now we have found the video stream in this file. Let's open up our
		 * decoder so it can do work.
		 */
		if (videoCoder.open() < 0)
			throw new RuntimeException("無法找到能開啟影片的解碼器，發生RuntimeException");

		int height = videoCoder.getHeight();
		int width = videoCoder.getWidth();
		if (width >= 1920 && height >= 1080) {
			resolution = new ArrayList<>(Arrays.asList("720X480", "1280X720", "1920X1080"));
		} else if (width >= 1280 && width < 1920 && height >= 720 && height < 1080) {
			resolution = new ArrayList<>(Arrays.asList("720X480", "1280X720"));
		} else if (width >= 720 && width < 1280 && height >= 480 && height < 720) {
			resolution = new ArrayList<>(Arrays.asList("720X480"));
		}
		return resolution;
	}

}