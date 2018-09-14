package com.video.cache;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@Component
public class FindFileCache {

	private static final Logger LOGGER = LoggerFactory.getLogger(FindFileCache.class);

	private static final String STR_PATH = "D:/M"; /* 初始資料夾位置 */
	private static final boolean string2Html = false; /* 是否將menu格式從String轉成Html格式 */
	/* Menu 起始文字 */
	private static StringBuilder uhtml = null;
	private static String finalHtml = "";
	private static final Map<String, String> fileMap = new HashMap<>();

	public static void reload() {
		File file = new File(STR_PATH); // 初始檔案資料夾
		findFile(file);
		/* 製作html String */
		uhtml = new StringBuilder().append("<ul>");
		listFilesForFolder(file);
		finalHtml = createHtmlFinalTag();
	}

	/**
	 * 檢驗副檔名是否符合
	 * 
	 * @param file
	 * @return
	 */
	public static boolean fileExtension(File file) {
		boolean check = false;
		String sub = file.getName().substring(file.getName().length()-4,file.getName().length());
		if (!file.isHidden() && ".MKV".equalsIgnoreCase(sub)
				|| ".MP4".equalsIgnoreCase(sub)
				|| ".WMV".equalsIgnoreCase(sub)
				|| ".AVI".equalsIgnoreCase(sub)) {
			check = true;
		}

		return check;
	}

	/**
	 * 掃描資料夾內所有檔案
	 * 
	 * @param file
	 */
	public static void findFile(File file) {
		try {
			if (file.listFiles() != null) {
				for (final File fileEntry : file.listFiles()) {
					if (fileEntry.exists()) {
						if (fileEntry.isDirectory()) {
							findFile(fileEntry);
						} else {
							if (fileExtension(fileEntry)) {
								fileMap.put(fileEntry.getName() + String.valueOf(fileEntry.hashCode()),
										fileEntry.getPath());
							}
						}
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("預載掃描資料夾發生 Exception :{}", e);
		}
	}

	/**
	 * String to Html 格式
	 * 
	 * @param unformattedXml
	 * @return
	 */
	public static String format(String unformattedXml) {
		try {
			final Document document = parseXmlFile(unformattedXml);
			OutputFormat format = new OutputFormat(document);
			format.setLineWidth(65);
			format.setIndenting(true);
			format.setIndent(2);
			Writer out = new StringWriter();
			XMLSerializer serializer = new XMLSerializer(out, format);
			serializer.serialize(document);
			return out.toString();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static Document parseXmlFile(String in) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(in));
			return db.parse(is);
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		} catch (SAXException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 取得資料夾內容
	 * 
	 * @param folder
	 */
	public static void listFilesForFolder(final File folder) {
		if (folder.listFiles() != null) {
			for (final File fileEntry : folder.listFiles()) {
				/*
				 * 略過系統預留檔
				 */
				if ("$RECYCLE.BIN".equals(fileEntry.getName()) && fileEntry.isHidden()
						|| "Recovery".equals(fileEntry.getName())
						|| "System Volume Information".equals(fileEntry.getName())) {
					continue;
				}
				if (fileEntry.isDirectory()) {
					if (fileEntry.list().length > 0) { /* 略過空資料夾 */
						/*
						 * 如上一層也是資料夾時在上層資料夾名稱後方加入<ul>
						 */
						if (uhtml != null && uhtml.length() > 10
								&& "</span>".equals(uhtml.substring(uhtml.length() - 7, uhtml.length()))) {
							uhtml.append("<ul>");
						}
						uhtml.append("<li><span style=\"color:#1e88e5\">").append(fileEntry.getName())
								.append("</span>");
						listFilesForFolder(fileEntry);
						/*
						 * 此資料夾是最後一個資料夾時加入</ul></li>
						 */
						if (fileEntry.getParentFile().list()[fileEntry.getParentFile().list().length - 1]
								.equals(fileEntry.getName())) {
							uhtml.append("</ul></li>");
						}
					}
				} else {
					/*
					 * STR_PATH.length() < fileEntry.getParent().length() ->
					 * 目標資料夾如第一個是檔案而非資料夾時不加入 <ul> 標籤
					 * fileEntry.getParentFile().list()[0].equals(fileEntry.
					 * getName()) -> 資料夾加入的第一個檔案時加入 <ul> 標籤
					 */
					if (STR_PATH.length() < fileEntry.getParent().length()
							&& fileEntry.getParentFile().list()[0].equals(fileEntry.getName())) {
						uhtml.append("<ul>");
					}

					/*
					 * FindFileCache.fileExtension -> 過濾檔案格式
					 */
					if (FindFileCache.fileExtension(fileEntry)) {
						uhtml.append("<li><a class=\"fileName\" data-param=\"").append(fileEntry.getName())
								.append("&amp;hash=").append(fileEntry.hashCode()).append("\">")
								.append(fileEntry.getName()).append("</a></li>");
					}

					/*
					 * STR_PATH.length() < fileEntry.getParent().length() ->
					 * 目標資料夾如第一個是檔案而非資料夾時不加入 </ul></li> 標籤
					 * 
					 * fileEntry.getParent().length() &&
					 * fileEntry.getParentFile().list()[fileEntry.getParentFile(
					 * ).list().length - 1] .equals(fileEntry.getName()) ->
					 * 資料夾加入的最後一個檔案加入 </ul></li> 標籤
					 */
					if (STR_PATH.length() < fileEntry.getParent().length()
							&& fileEntry.getParentFile().list()[fileEntry.getParentFile().list().length - 1]
									.equals(fileEntry.getName())) {
						uhtml.append("</ul></li>");
					}
				}
			}
		}
	}

	/**
	 * 資料夾HtmlTag結尾判斷(判斷最後為資料夾還是檔案)
	 * 
	 * @return
	 */
	public static String createHtmlFinalTag() {
		String finalHtml = uhtml.toString();
		if ("</li>".equals(uhtml.substring(uhtml.length() - 5, uhtml.length()))) {
			finalHtml = uhtml.append("</ul>").toString();
		} else if ("</li></ul>".equals(uhtml.substring(uhtml.length() - 10, uhtml.length()))) {
			finalHtml = uhtml.substring(0, uhtml.length() - 10);
		}
		System.out.println(finalHtml);
		return finalHtml;
	}

	/* 供其他Class呼叫預載參數 */

	public static Map<String, String> getFileMap() {
		return fileMap;
	}

	public static String getPath() {
		return STR_PATH;
	}

	public static boolean getString2Html() {
		return string2Html;
	}

	public static String getFinalHtml() {
		if (FindFileCache.getString2Html()) {
			finalHtml = format(finalHtml);
		}
		return finalHtml;
	}
}
