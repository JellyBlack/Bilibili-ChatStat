package script;

/**
 * ��������
 */
public class Config {

	// �Ƿ������޸�����
	public static boolean ALLOW_MODIFY = true;

	// �߼���Ļ�ϲ�����ʽΪ{"������ʽ","�滻����"}����{"^OH{2,}$","OHHHH"}��Ҳ�����ڡ��滻���֡������ע�͡�
	// ����Ԥ��Ĺ���⣬ʹ���߿����Զ��塣
	public static String[][] ADVANCED_MATCH_SET = { { "^[Oo][Hh]{2,}$", "OHHHH" }, { "^23{2,}$", "23333" },
			{ "��+", "��" }, { "��+", "��" }, { "\\!+", "\\!" }, { "��+", "��" }, { ".*[Aa].*[Ww].*[Ss].*[Ll].*", "awsl" },
			{ ".*��������.*", "awsl" }, { ".*��ΰ����.*", "awsl" }, { ".*��ΰ˯��.*", "awsl" }, { ".*������¹.*", "awsl" },
			{ ".*������¿.*", "awsl" }, { ".*��������.*", "awsl" }, { ".*��������.*", "awsl" }, { ".*��������.*", "awsl" },
			{ ".*����˯��.*", "awsl" }, { ".*��ΰ��¹.*", "awsl" }, { ".*��ΰ��¿.*", "awsl" }, { ".*��ΰ����.*", "awsl" },
			{ ".*��ΰ����.*", "awsl" }, { ".*��������.*", "awsl" } };

	// ��������
	public static class public_config {
		public static boolean IGNORE_CASES = true;// ���Դ�Сд
		public static boolean TO_SBC = false;// ת��Ϊȫ���ַ���Ϊ����csv��ʽ���󣬲����Ƿ�����ѡ���Ƕ��Ŷ��ᱻת��Ϊȫ�Ƕ��ţ�
		public static boolean IGNORE_SPACES = true;// ɾ����β�ո�
		public static boolean SPLIT_CHATS = true;// ��ͼ����Ļ���Ϊ����Ƭ�Σ��硰awsl awsl awsl������Ϊ��awsl�������� �� �� �� ��������Ϊ������
		public static boolean ADVANCED_MATCH = true;// �߼���Ļƥ�俪��
		public static boolean MARK_ONCE = true;// һ���˷��Ķ�����ͬ��Ļֻ��һ�Σ�����ͬ��Ļ��֧��ǰ����ѡ���ǰ����ѡ�����Ϻ����ж��Ƿ��ǡ���ͬ��Ļ����
		public static int OUTPUT_STYLE = 1;// �����ʽ��0Ϊswing���չʾ��1Ϊcsv�ļ�չʾ
	}

	// ��ȡ��Ƶ��Ļ����
	public static class spider_config {
		public static int mode = 0;// ģʽ��0Ϊ��ȡ����Ƶ��1Ϊ��ȡ����Ƶ��2Ϊ��ȡָ��up����ȫ����Ƶ
		public static int[] avs;// av���б�
		public static int uid = 0;// UP����UID
	}

	// ֱ����ץȡ��Ļ����
	public static class live_config {
		public static int ROOM = -1;// ֱ����
		public static int DELAY = 1000;// ������ȡ��Ļ����ʱ
		public static boolean STATUS = false;// �Ƿ�����ִ��
		public static boolean AUTO_DELAY = true;// �Զ�������ʱ
		public static long START_TIME = 0;// ��ʼ��ȡ��ʱ���
	}

	// ��һ��ѡ�������
	public static class tab1 {
		public static int RANK_LIMIT = -1;// �������������Ļ��������չʾǰ��������������Ϊ-1��ȫ������
	}

	// �ڶ���ѡ�������
	public static class tab2 {
		public static float LENGTH = 15; // �������ȣ���λΪ��
		public static float START_TIME = 0; // ��ʼʱ�䣬����Ϊ0����ͷ��ʼ
		public static float END_TIME = -1; // ��ֹʱ�䣬����Ϊ-1������Ƶĩβ
	}

	// ������ѡ�������
	public static class tab3 {
		public static boolean CRC32 = false; // �Ƿ����CRC32���㣨ʮ������ʱ�䣬�����Ƽ���
	}

	// ������ѡ�������
	public static class tab6 {
		public static int LENGTH = 1800; // �������ȣ���λΪ��
		public static int START_TIME = 0; // ��ʼʱ�䣬����Ϊ0����00:00:00��ʼ
		public static int END_TIME = -1; // ��ֹʱ�䣬����Ϊ-1����һ�����
	}
}
