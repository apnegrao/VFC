package mobihoc.session;

/** class containing all the consistency parameters appliable to an object of the domain belonging to a certain consistency zone **/
public class KVec implements java.io.Serializable {
	
	private static final long serialVersionUID = 1L;
	
	/** time without updates (in number of rounds, since it is assumed that the consistency parameter Theta should be a multiple of the duration of each round) **/
	private int _theta;
	/** number of updates skipped **/
	private int _sigma;
	/** difference in relation to last seen value (in %) **/
	private float _niu;
	
	
	public KVec() {}
	
	public KVec(int theta, int sigma, float niu) {
		this._theta = theta;
		this._sigma = sigma;
		this._niu = niu;
	}
	
	/** returns the value of the consistency parameter Theta - maximum time inteval allowed without updates (in number of rounds, since it is assumed that the consistency parameter Theta should be a multiple of the duration of each round)  **/
	public int getTheta() {
		return _theta;
	}
	
	/** returns the value of the consistency parameter Sigma - maximum number of updates allowed to be skipped before a new update should be sent to the client **/
	public int getSigma() {
		return _sigma;
	}
	
	/** returns the value of the consistency parameter Nu - maximum difference in relation to the last seen value (in %)  before a new update should be sent to the client**/
	public float getNiu() {
		return _niu;
	}

}