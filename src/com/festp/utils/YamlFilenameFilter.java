package com.festp.utils;

import java.io.File;
import java.io.FilenameFilter;

public class YamlFilenameFilter implements FilenameFilter {
	@Override
	public boolean accept(File dir, String name) {
		return name.endsWith(".yml");
	}
}
