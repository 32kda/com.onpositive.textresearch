package com.onpositive.semantic.categorization.core;

public interface ISystemConfiguration {

	Categories getCategories();
	
	SubSystemConfiguration[] getEngineConfigurations();
	
	SubSystemConfiguration getOverrideConfiguration(String engineId);

}
