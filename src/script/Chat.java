package script;

import java.util.ArrayList;

import gui.MainGui;

/**
 * ��Ļ��ʵ����
 */
public class Chat {
	private ArrayList<String> chats;
	private ArrayList<String> users;
	private ArrayList<Float> time;
	private ArrayList<Long> date;

	/**
	 * ������Ļ
	 * 
	 * @param chats ��Ļ
	 * @param users �û�
	 * @param time  ����ʱ�䣨ָλ����Ƶ��ʲôʱ��
	 * @param date  ����ʱ�䣨ʱ�����
	 */
	public Chat(ArrayList<String> chats, ArrayList<String> users, ArrayList<Float> time, ArrayList<Long> date) {
		this.chats = chats;
		this.users = users;
		this.time = time;
		this.date = date;
	}

	/**
	 * ��ȡ��Ļ�б�
	 * 
	 * @return ��Ļ�б�
	 */
	public ArrayList<String> getChats() {
		return chats;
	}

	/**
	 * ���õ�Ļ�б�
	 * 
	 * @param chats ��Ļ�б�
	 */
	public void setChats(ArrayList<String> chats) {
		this.chats = chats;
	}

	/**
	 * ��ȡ�û��б�
	 * 
	 * @return �û��б�
	 */
	public ArrayList<String> getUsers() {
		return users;
	}

	/**
	 * �����û��б�
	 * 
	 * @return �û��б�
	 */
	public void setUsers(ArrayList<String> users) {
		this.users = users;
	}

	/**
	 * ��ȡʱ���б�
	 * 
	 * @return ʱ���б�
	 */
	public ArrayList<Float> getTime() {
		return time;
	}

	/**
	 * ����ʱ���б�
	 * 
	 * @return ʱ���б�
	 */
	public void setTime(ArrayList<Float> time) {
		this.time = time;
	}

	/**
	 * ��ȡ�����б�
	 * 
	 * @return �����б�
	 */
	public ArrayList<Long> getDate() {
		return date;
	}

	/**
	 * ���������б�
	 * 
	 * @return �����б�
	 */
	public void setDate(ArrayList<Long> date) {
		this.date = date;
	}

	/**
	 * ׷���µĵ�Ļ����ԭ����
	 * 
	 * @param new_chat �µ�Ļ
	 */
	public void append(Chat new_chat) {
		chats.addAll(new_chat.getChats());
		users.addAll(new_chat.getUsers());
		time.addAll(new_chat.getTime());
		date.addAll(new_chat.getDate());
	}

	/**
	 * ׷���µĵ�Ļ����ԭ����
	 * 
	 * @param chat  �µ�Ļ
	 * @param user  ���û�
	 * @param time_ ��ʱ��
	 * @param date_ ������
	 */
	public void append(String chat, String user, Float time_, Long date_) {
		chats.add(chat);
		users.add(user);
		time.add(time_);
		date.add(date_);
	}

	/**
	 * ɾ����β�ַ�
	 */
	public void trim() {
		for (int i = 0; i < chats.size(); i++) {
			MainGui.getInstance().refreshProgressBar(i);
			chats.set(i, chats.get(i).trim());
		}
	}

	/**
	 * ת��Ϊȫ���ַ�
	 */
	public void to_sbc() {
		for (int i = 0; i < chats.size(); i++) {
			MainGui.getInstance().refreshProgressBar(i);
			char[] c = chats.get(i).toCharArray();
			for (int j = 0; j < c.length; j++) {
				if ((c[j] >= 33 && c[j] <= 47) || (c[j] >= 58 && c[j] <= 64) || (c[j] >= 91 && c[j] <= 96)
						|| (c[j] >= 123 && c[j] <= 126)) {
					c[j] = (char) (c[j] + 65248);
				}
			}
			chats.set(i, new String(c));
		}
	}

	/**
	 * ��Ļ���
	 * 
	 * @param ignore_cases �Ƿ���Դ�Сд
	 */
	public void split_chats(boolean ignore_cases) {
		outter: for (int i = 0; i < chats.size(); i++) {
			MainGui.getInstance().refreshProgressBar(i);
			// ��ͼɾ���ո�
			String chat = chats.get(i).replaceAll(" ", "");
			if (ignore_cases) {
				chat.toLowerCase();
			}
			char[] chars = chat.toCharArray();
			tick: for (int j = 1; j < chat.length(); j++) {
				if (chars.length % j != 0) {
					continue tick;
				}
				for (int k = 0; k < chars.length / j - 1; k++) {
					for (int offset = 0; offset < j; offset++) {
						if (!(chars[offset] == chars[offset + j * (k + 1)])) {
							continue tick;
						}
					}
				}
				StringBuilder builder = new StringBuilder();
				for (int l = 0; l < j; l++) {
					builder.append(chars[l]);
				}
				chats.set(i, builder.toString());
				continue outter;
			}
		}
	}

	/**
	 * �߼���Ļ�ϲ�
	 * 
	 * @param ignore_cases �Ƿ���Դ�Сд
	 * @param set          �ϲ������
	 */
	public void advanced_match(boolean ignore_cases, String[][] set) {
		for (int i = 0; i < set.length; i++) {
			MainGui.getInstance().refreshProgressBar(i);
			for (int j = 0; j < chats.size(); j++) {
				String text;
				if (ignore_cases) {
					text = chats.get(j).toLowerCase();
				} else {
					text = chats.get(j);
				}
				chats.set(j, text.replaceAll(set[i][0], set[i][1]));

			}
		}
	}

	/**
	 * ����һ���˷��Ķ�����ͬ��Ļֻ����һ��
	 * 
	 * @param ignore_cases �Ƿ���Դ�Сд
	 */
	public void mark_once(boolean ignore_cases) {
		ArrayList<String> chats_temp = new ArrayList<>();
		ArrayList<String> users_temp = new ArrayList<>();
		ArrayList<Float> time_temp = new ArrayList<>();
		ArrayList<Long> date_temp = new ArrayList<>();
		for (int i = 0; i < chats.size(); i++) {
			chats_temp.add(chats.get(i));
			users_temp.add(users.get(i));
			time_temp.add(time.get(i));
			date_temp.add(date.get(i));
		}
		for (int i = 0; i < chats.size(); i++) {
			MainGui.getInstance().refreshProgressBar(i);
			String user = users.get(i);
			for (int j = i + 1; j < chats.size(); j++) {
				if (user.equals(users.get(j))) {
					if (ignore_cases) {
						if (chats.get(i).toLowerCase().equals(chats.get(j).toLowerCase())) {
							chats_temp.set(j, "");
						}
					} else {
						if (chats.get(i).equals(chats.get(j))) {
							chats_temp.set(j, "");
						}
					}
				}
			}
		}
		chats.clear();
		users.clear();
		time.clear();
		date.clear();
		for (int i = 0; i < chats_temp.size(); i++) {
			if (!chats_temp.get(i).equals("")) {
				chats.add(chats_temp.get(i));
				users.add(users_temp.get(i));
				time.add(time_temp.get(i));
				date.add(date_temp.get(i));
			}
		}
	}

	/**
	 * ��ȡ��Ļ����
	 * 
	 * @return ����
	 */
	public int getCount() {
		return chats.size();
	}
}
