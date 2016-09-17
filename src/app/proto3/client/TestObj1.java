package app.proto3.client;

/* [Serializable] */
public class TestObj1 {

	public static final long NULL_ID = 0;

	private long _id;
	private byte[] _data;

	public TestObj1() {
		_id = NULL_ID;
	}

	public TestObj1(long id) {
		_id = id;
		_data = null;
	}

	public TestObj1(long id, byte[] data) {
		_id = id;
		_data = data;
	}

	public long getId() {
		return _id;
	}

	public void setId(long id) {
		this._id = id;
	}

	public byte[] getData() {
		return _data;
	}

	public void setData(byte[] data) {
		_data = data;
	}
}
