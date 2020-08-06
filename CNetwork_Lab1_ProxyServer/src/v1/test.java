package v1;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class test {
	/**
	 * 正则替换所有特殊字符
	 * 
	 * @param orgStr
	 * @return
	 */
	public static String replaceSpecStr(String orgStr) {
		String regEx = "[\\s~·`!！@#￥$%^……&*（()）\\-——\\-_=+【\\[\\]】｛{}｝\\|、\\\\；;：:‘'“”\"，,《<。.》>、/？?]";
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(orgStr);
		return m.replaceAll("");
	}

	public static void main(String[] args) throws IOException {
		String a ="dasdasd....";

	}

}
