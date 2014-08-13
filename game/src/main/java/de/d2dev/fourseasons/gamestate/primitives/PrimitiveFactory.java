package de.d2dev.fourseasons.gamestate.primitives;

public interface PrimitiveFactory {
	
	public BooleanObject createBoolean(boolean b);
	public IntegerObject createInteger(int i);
	public DoubleObject createDouble(double d);
	public FloatObject createFloat(float f);
	public StringObject createString(String s);
}

