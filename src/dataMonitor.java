import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import Trie.Emit;
import Trie.Trie;
import Trie.Trie.TrieBuilder;

/**
 * 数据监测关键字匹配接口
 * 
 * @author xutao
 * @email xutao199218@163.com
 * @since 2016年1月19日 下午2:16:36
 * @version 1.0
 */

public class dataMonitor {

	private static TrieBuilder trieBuilder;
	private static Trie trie;
	private static Set<String> configSetLocal;
	private static Map<Integer, Set<String>> configMapLocal;
	private static long bytesTotal = 0;

	public static int configCounter() {
		return configSetLocal.size();
	}

	/**
	 * 关键词加载
	 * 
	 * @param configMap
	 *            ：所有的配置Id和配置内容
	 * */
	public static void keywordLoader(Map<Integer, Set<String>> configMap) {

		configMapLocal = new HashMap<Integer, Set<String>>();
		configSetLocal = new HashSet<String>();

		configMapLocal.putAll(configMap);
		trieBuilder = Trie.builder();

		Iterator<Entry<Integer, Set<String>>> iter = configMap.entrySet()
				.iterator();

		while (iter.hasNext()) {
			Entry<Integer, Set<String>> entry = iter.next();
			Set<String> configSet = entry.getValue();// 配置内容
			for (String str : configSet) {
				trieBuilder.addKeyword(str.trim());
				configSetLocal.add(str.trim());
			}
		}

		trie = trieBuilder.build();
	}

	/**
	 * 关键词匹配
	 * 
	 * @param context
	 *            : 文本内容
	 * @return List<Integer>: 匹配到的配置结果列表
	 * */
	public static List<Integer> keywordMatcher(String context) {

		Collection<Emit> emits = trie.parseText(context);
		Set<String> resultSet = new HashSet<String>();
		for (Emit e : emits) {
			String str = e.getKeyword();
			resultSet.add(str.trim());
		}

		List<Integer> configResultList = new ArrayList<Integer>();

		Iterator<Entry<Integer, Set<String>>> iter = configMapLocal.entrySet()
				.iterator();
		while (iter.hasNext()) {
			Entry<Integer, Set<String>> entry = iter.next();
			Integer configId = entry.getKey();// 配置ID
			Set<String> value = entry.getValue();// configSet
			if (resultSet.containsAll(value)) {
				configResultList.add(configId);
			}
		}
		return configResultList;
	}

	/**
	 * 模拟配置构造
	 * 
	 * @param configMap
	 * */
	public static void keywordsIniter(Map<Integer, Set<String>> configMap) {
		Set<String> configSet = new HashSet<String>();
		configSet.add("周杰伦");
		configSet.add("昆凌");
		configMap.put(1, configSet);

		configSet = new HashSet<String>();
		configSet.add("周杰伦");
		configSet.add("昆凌");
		configSet.add("王力宏");
		configMap.put(2, configSet);

		configSet = new HashSet<String>();
		configSet.add("中国");
		configMap.put(3, configSet);

		configSet = null;
	}

	/**
	 * 从文件加载配置
	 * 
	 * @param src
	 *            文件
	 * @param 配置的映射
	 *            ，常驻内存
	 * */
	public static void keywordsIniter(File src,
			Map<Integer, Set<String>> configMap) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(src));
			String line = "";
			while ((line = br.readLine()) != null) {
				Set<String> set = new HashSet<String>();
				String[] tmp = line.split(" ");
				String[] suffix = tmp[1].split(",");
				for (String str : suffix) {
					set.add(str);
				}
				// System.out.println(tmp[0] + " " + set);
				configMap.put(Integer.parseInt(tmp[0].trim()), set);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * 一次性读取文件的所有内容
	 * 
	 * @throws UnsupportedEncodingException
	 * 
	 * @Param file
	 * */
	public static String readFileFull(File file)
			throws UnsupportedEncodingException {
		Long filelength = file.length(); // 获取文件长度
		bytesTotal += filelength;
		byte[] filecontent = new byte[filelength.intValue()];
		try {
			FileInputStream in = new FileInputStream(file);
			in.read(filecontent);
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new String(filecontent, "GBK"); // 返回文件内容,默认编码
	}

	/**
	 * 遍历指定文件夹
	 * 
	 * @param filePath
	 * */
	public static int traverseTheFolder(String filePath) {
		File rootDir = new File(filePath);
		File[] files = rootDir.listFiles();
		if (files == null) {
			System.out.println("No such dir");
			return -1;
		}
		for (int i = 0; i < files.length; i++) {
			String context = "";
			try {
				context = readFileFull(files[i]);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// 关键词匹配
			List<Integer> matchConfigIdList = keywordMatcher(context);
			// debug辅助
			// keywordDebugger();
			// System.out.println("In File==["+ files[i].getAbsolutePath()
			// +"], IdList == " + matchConfigIdList);
		}
		return files.length;
	}

	/**
	 * 测试函数
	 * */
	public static void main(String[] args) {

		Map<Integer, Set<String>> configMap = new HashMap<Integer, Set<String>>();

		long start = System.currentTimeMillis();

		keywordsIniter(configMap);
		keywordLoader(configMap);

		String context = "大家好,当大家收到这封 Email 时，公司已经正式同意我提早退休了，时间到 3月底，这也是我告别上班族的开始，告别 27年的上班族生涯，以後改换乐活(LOHAS)生活，可以依照自己的意愿来过日子；而因3月我要开始筹画未来的生活，出现在公司时间相对会减少，所以透过这封 Email 跟大家 Say Goobye。以後，不用想我，就让我无牵挂的游回大江吧。  	回想当初 81年来xx上班是怎样的心情，日子久的自己都快忘记了，慢慢回想起，是甘先生在某一天晚上打电话通知我来面试，接着就顺其自然的开始xx的上班生活，来报到的这一天，倒是一直记得很清楚，因为他好记又有含意，是 4 月1日，敏感一点的人马上就反应这一天不就是愚人节? 没错，xx第一天上班就是从愚人节这天开始，到3月31日结束(愚人节前夕)，前前後後整整19年头，首尾相连的 19年，也代表27年的朝九晚五上班族生活划下句号：中国人对於数字特别敏感，逢 4 不用，而 9 是九九天尊，在古代只能是皇帝层级才能使用，而有幸人生中重要的一段也是精力最好的一段(32~51岁)的句点逢到「9」，这给我的感觉就是老天赐於我的一种幸运，一种好运。  	人生的长短，有人说：「人生苦短」，苦的短那是件好事，苦的长就不好了。一个人有几个 19，是人都是在这时间长河中漂流着，没有谁能多出谁多少日子，只有百步与五十步差异而已。人生有多少转折，也是不多的，而退休这事对每个上班族都是一件堪称转折的事情，「转折」也意味着「契机」，而工作能到退休且身体状况还可以，也是一种福气。能这样退休，我是很高兴的、很兴奋的 (害我 2.26晚上没能准时10点睡着，起床跟友人网路分享)。  	回顾这段xx上班族日子，不免老套的要感谢一些人!  (没提到的人不用苦恼，私下再感谢)  	第一感谢的是甘先生，是他引进我进xx，算的得上是我的贵人。而令我佩服的人是已经离开的「陈xx」陈先生，佩服他对工作的仔细与和善对同事的沟通与诱导，曾经跟他有段接触，是段美好的回忆。已经退休在家休养的庄先生也是位令我佩服的长者，他对工作的认真要求与对员工的和善，有共事过的同仁都留下深刻印象。而半退休的王先生，他的宽容与眼光，在他担任总经理期间，与他接触过程也是段美好的回忆。谢先生，来到公司时，我已经待在里头，对这位曾经的总经理，现在的董事长，属「海归派」对於科技应用的纯孰与认同，对事情的探讨细致，也是位让我印象深刻的长官。  	林林总总写了一堆，还未写到人生有缘一道共事的冯小姐，感谢她来到电脑室这段日子，忍受我的脾气，做好分内的每一件事情，深庆当初推荐她来电脑室。未来的日子，也期许她能继续保持这态度。  	最後，也是晚期两年多接触的蒋先生，感谢他这两年多的教诲与触动，让我认清人生。总之，这段在xx日子(19年)付出过、努力过、低潮过、高潮过，虽没有值得大书特写的事情，但他将是我回顾人生时精采的一段，不後悔当初的来到，同样也不後悔今日的离开。告别朝九晚五的上班族生涯，下一个 19年正等着我去添妆加墨，未来的日子将会是另一场精采!!!  	xx现在的夥伴们，请珍惜现在的自己，不要让劳累拖垮健康，每工作40分钟记得起来喝喝水、走走动，不愿意那天听到有那位老同事，在工作岗位上因劳累而晕倒了。  	感恩大家，愿意看我的唠叨，祝福大家虎年幸福而乐在工作!!!  	antma 敬上 2010.3.1  	antma888@gmail.com 是我以後的联络方式  	挑选一张代表新的一年开始摄影作品与大家分享  	题名：元宵炸土地公  	时间：2010.2.28  	地点：台北内湖  	<img	src=http://clie.ws/bbs/public/style_extra/mime_types/gif.gif	align=center>   _MG_7632.jpg   253.64K    6 下载数";
		List<Integer> matchConfigIdList = keywordMatcher(context);
		System.out.println(matchConfigIdList);

		long end = System.currentTimeMillis();

		System.out.println("time take : " + ((end - start) / 1000) + "s");

		// 构造配置
		// keywordsIniter(configMap);
		/*
		 * keywordsIniter(new File("E://data_of_platform//config.txt"),
		 * configMap); System.out .println("config init time == [" +
		 * (System.currentTimeMillis() - timeStart) / 1000 + "] seconds" );
		 * 
		 * // 配置启动加载 keywordLoader(configMap); System.out
		 * .println("AC construct == [" + (System.currentTimeMillis() -
		 * timeStart) / 1000 + "] seconds, config total count=" +
		 * configCounter());
		 * 
		 * final String filePath = "E://data_of_platform//"; // 遍历文件夹下的文件进行匹配
		 * int fileCount = traverseTheFolder(filePath);
		 * System.out.println("AC match == [" + (System.currentTimeMillis() -
		 * timeStart) / 1000 + "] seconds, handle [" + fileCount + "] files, ["
		 * + bytesTotal/(1024*1024) + "] MB data" );
		 */

	}
}
