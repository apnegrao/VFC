package mobihoc.session;

public abstract class PositionableDataUnit extends DataUnit {

	public abstract int getPosX();

	public abstract int getPosY();

	public int compareWithPivot(DataUnit pivot) {
		PositionableDataUnit p = (PositionableDataUnit)pivot;
		return (Math.max(Math.abs(getPosX() - p.getPosX()), Math.abs(getPosY() - p.getPosY())));
	}

}
