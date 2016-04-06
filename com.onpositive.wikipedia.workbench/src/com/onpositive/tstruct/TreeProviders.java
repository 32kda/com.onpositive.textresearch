package com.onpositive.tstruct;

import java.util.HashMap;

public class TreeProviders {

	protected HashMap<String, ITreeProvider>providers=new HashMap<String,ITreeProvider>();
		
	public void register(ITreeProvider provider){
		providers.put(provider.id(), provider);
	}
}
