package com.video.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import com.video.cache.FindFileCache;
import com.video.exception.ConvertedException;
import com.video.upload.ConverVideoTest;
import com.video.utils.Contants;
import com.video.utils.MultipartFileSender;

@Controller
public class videoController {

	/* Print Log */
	private static final Logger LOGGER = LoggerFactory.getLogger(videoController.class);

	/* Request */
	private static final String REQUEST_INDEX = "/";
	private static final String REQUEST_FOLDER = "/view";
	private static final String REQUEST_VIEW = "file";

	/* Response */
	private static final String RESPONSE_INDEX = "Index";
	private static final String RESPONSE_VIDEO = "video";

	/* Attribute */
	private static final String ATTR_INDEXlIST = "folderAndFile"; /* 資料夾與檔名 */
	private static final String ATTR_FILENAME = "fileName"; /* 檔名 */

	/**
	 * 首頁
	 * 
	 * @return
	 */
	@RequestMapping(REQUEST_INDEX)
	public String goIndex(HttpServletRequest request) {
		String finalHtml = FindFileCache.getFinalHtml();
		request.setAttribute(ATTR_INDEXlIST, finalHtml);
		return RESPONSE_INDEX;
	}

	/**
	 * 轉入影片撥放頁面
	 * 
	 * @param fileName
	 * @param request
	 * @return
	 */
	@RequestMapping(REQUEST_FOLDER)
	public String forward(@RequestParam(required = false, value = "v") String fileName, HttpServletRequest request) {
		request.setAttribute(ATTR_FILENAME, fileName);
		return RESPONSE_VIDEO;
	}

	/**
	 * 取得影片檔案
	 * 
	 * @param name
	 * @param response
	 * @param request
	 */
	@RequestMapping(value = REQUEST_VIEW, method = RequestMethod.GET)
	public void getPreview(@RequestParam(value = "v") String name, @RequestParam(value = "hash") String hashCode,
			HttpServletResponse response, HttpServletRequest request) {
		try {
			Map<String, String> fileMap = FindFileCache.getFileMap();
			String path = fileMap.get(name + hashCode);
			LOGGER.info("取得完整檔案Path:{}", path);
			MultipartFileSender.fromFile(new File(path)).with(request).with(response).serveResource();
		} catch (Exception e) {
			LOGGER.error("find error Exception : {}", e.getMessage());
		}
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST)
	public String update(@RequestParam("file") CommonsMultipartFile file, HttpServletRequest req,
			HttpServletRequest request) throws Exception {
		System.out.println("進入addVideo影片上傳Controller");

		if (file.getSize() != 0) {
			/* 上傳的多格式的影片檔-作為暫存路徑儲存，轉檔以後刪除-路徑不能寫// */
			String path = Contants.tempPath;

			File TempFile = new File(path);
			if (TempFile.exists()) {
				if (TempFile.isDirectory()) {
					System.out.println("此資料夾存在。");
				} else {
					System.out.println("同名的資料夾存在，無法建立此資料夾。");
				}
			} else {
				System.out.println("此資料夾不存在，建立該資料夾。");
				TempFile.mkdirs();
			}

			/* 取得上傳時的檔名 */
			String filename = file.getOriginalFilename();

			/* 取得文件副檔名 */
			String filename_extension = filename.substring(filename.lastIndexOf(".") + 1);
			System.out.println("影片的副檔名:" + filename_extension);

			/* 時間戳做新的檔名，避免中文亂碼-重新生成filename 註解此部分就會使用原檔名 */
			// long filename1 = new Date().getTime();
			// filename = Long.toString(filename1) + "." + filename_extension;

			/* 去掉副檔名 */
			String filename2 = filename.substring(0, filename.lastIndexOf("."));
			System.out.println("影片名為:" + filename2);

			/* 原始影片位置+重新命名後的影片名+副檔名 */
			String yuanPATH = (path + filename);

			System.out.println("影片的完整檔名:" + filename);
			System.out.println("原始影片位置為:" + yuanPATH);

			/* 上傳到Local硬碟/伺服器 */
			try {
				System.out.println("寫入Local影片/伺服器");
				InputStream is = file.getInputStream();
				OutputStream os = new FileOutputStream(new File(path, filename));
				int len = 0;
				byte[] buffer = new byte[2048];

				while ((len = is.read(buffer)) != -1) {
					os.write(buffer, 0, len);
				}
				os.close();
				os.flush();
				is.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			System.out.println("========上傳完成，開始使用轉檔工具=======");
			/*
			 * 使用轉檔機制flv mp4 f4v m3u8 webm ogg放行直接播放，
			 * asx，asf，mpg，wmv，3gp，mov，avi，wmv9，rm，rmvb等進行其他轉檔為mp4
			 */
			try {
				if (filename_extension.equals("avi") || filename_extension.equals("rm")
						|| filename_extension.equals("rmvb") || filename_extension.equals("wmv")
						|| filename_extension.equals("3gp") || filename_extension.equals("mov")
						|| filename_extension.equals("flv") || filename_extension.equals("ogg")

				) {

					ConverVideoTest c = new ConverVideoTest();
					c.run(yuanPATH); /* 使用轉檔 */
					System.out.println("===================所有解析度完成轉檔=======================");
					request.setAttribute("successful", new ArrayList<>(Arrays.asList("轉檔完成")));
				}
			} catch (ConvertedException e) {
				request.setAttribute("errors", new ArrayList<>(Arrays.asList("轉檔編碼不支援，轉檔發生錯誤")));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			// /* 取得轉檔後的mp4檔名 */
			// String Mp4path = "E://Projectpicture/websiteimages/finshvideo/";
			// filename2 = filename2 + ".mp4";
			// String NewVideopath = Mp4path + filename2;
			// System.out.println("新影片的url:" + NewVideopath);
			//

			/* 刪除暫存檔 */
			File file2 = new File(path);
			if (!file2.exists()) {
				System.out.println("沒有此檔案");
			}
			if (!file2.isDirectory()) {
				System.out.println("沒有此資料夾");
			}
			String[] tempList = file2.list();
			File temp = null;
			for (int i = 0; i < tempList.length; i++) {
				if (path.endsWith(File.separator)) {
					temp = new File(path + tempList[i]);
				} else {
					temp = new File(path + File.separator + tempList[i]);
				}
				if (temp.isFile() || temp.isDirectory()) {
					if (temp.delete()) {
						System.out.println("暫存檔: " + temp.getPath() + " 移除成功");
					} else {
						System.out.println("暫存檔: " + temp.getPath() + " 移除失敗");
					}
					/* 刪除資料夾裡面的檔案 */
				}
			}
			FindFileCache.reload(); /* 重新讀取menu */

			/* 實作用戶類  */
			// tb_resource resource = new tb_resource();

			/* 取得談寫的相關訊息 */
			// String title = request.getParameter("title");
			// String writer = request.getParameter("writer");
			// int state = Integer.parseInt(request.getParameter("state"));
			// String time = request.getParameter("time");
			// int clicks = Integer.parseInt(request.getParameter("clicks"));
			// int grade = Integer.parseInt(request.getParameter("grade"));
			// String subclass = request.getParameter("subclass");
			// int uid = Integer.parseInt(request.getParameter("uid"));

			/* 數據庫存儲訊息 */
			// resource.setTitle(title);
			// resource.setWriter(writer);
			// resource.setTime(time);
			// resource.setClicks(clicks);
			// resource.setGrade(grade);
			// resource.setSubclass(subclass);
			// resource.setState(state);
			// resource.setUid(uid);
			// resource.setSuffix(filename2);
			// resource.setUrl(NewVideopath); /* 已轉檔後的影片存放位置 */

			/* 實現對資料庫的更新 */
			// int n = 0;
			// n = tb_resourceService.insertResource(resource);
			//
			// if (n != 0) {
			// return new
			// ModelAndView("back/public/success").addObject("notice",
			// "resourceList?uid=" + uid + "&grade=-1&state=-1&subclass=" +
			// subclass);
			// } else {
			// return new ModelAndView("back/public/fail").addObject("notice",
			// "resourceList?uid=" + uid + "&grade=-1&state=-1&subclass=" +
			// subclass);
			// }
		}
		return goIndex(request);
	}
}
