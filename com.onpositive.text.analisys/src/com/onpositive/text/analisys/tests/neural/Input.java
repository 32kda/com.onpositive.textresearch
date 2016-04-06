package com.onpositive.text.analisys.tests.neural;

import java.util.Arrays;

public class Input {

	private final double[] data;

	public Input(double[] data) {
		super();
		this.data = data;
	}
	
	public double[] getData() {
		return data;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(data);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Input other = (Input) obj;
		if (!Arrays.equals(data, other.data))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Input [data=" + Arrays.toString(data) + "]";
	}
	
	
	
}
