MozzAndroidUtils
===================

HttpUtils �÷�
-------------------
'HttpUtils httpUtils = new HttpUtils();
		httpUtils.get("http://www.baidu.com", new HttpListener() {

			@Override
			public void onSuccess(HttpResponse response) {
				if (response.status == HTTP_OK)
					System.out.println(response.html);

			}

			@Override
			public void onFail(HttpResponse response) {
				// TODO Auto-generated method stub

			}
		});
'

DB�÷�
--------------------
��Mozz������еı��У����뺬���ֶ�"_id",��ʾ������

���ȼ̳�Model�࣬���������Ǳ���ÿ�е����ݣ��������������ֶ�һ�µ����ԣ�_id������ӣ���
'class Student extends Model {
	private String name;

	public void setName(String nm) {
		this.name = nm;
	}
}'
���ȼ̳�Eloquent, �����Ĺ����Ǳ��� + Eloquent
'class StudentsEloquent extends Eloquent<Student>{

}
'

�˺�Ϳ��Ե����ˡ�

��ѯ���У�
'
StudentsEloquent students = new StudentsEloquent();
Cursor = students.all();
'
����id,������
'Student student = students.find(1, new Student());
student.name = "zhangdao";
student.save();
'
