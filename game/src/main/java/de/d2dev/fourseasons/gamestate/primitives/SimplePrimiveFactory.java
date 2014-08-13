package de.d2dev.fourseasons.gamestate.primitives;

public class SimplePrimiveFactory implements PrimitiveFactory {

	@Override
	public BooleanObject createBoolean(boolean b) {
		return new BooleanObject(b);
	}

	@Override
	public IntegerObject createInteger(int i) {
		return new IntegerObject(i);
	}

	@Override
	public DoubleObject createDouble(double d) {
		return new DoubleObject(d);
	}

	@Override
	public FloatObject createFloat(float f) {
		return new FloatObject(f);
	}

	@Override
	public StringObject createString(String s) {
		return new StringObject(s);
	}
}
