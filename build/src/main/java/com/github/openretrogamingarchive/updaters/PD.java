package com.github.openretrogamingarchive.updaters;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.github.openretrogamingarchive.helpers.HTTP;
import com.github.openretrogamingarchive.helpers.HTTP.ZIPMode;
import com.github.openretrogamingarchive.helpers.Util;

public class PD extends Updater {
	private static final String PD_DIR = "PleasureDome";

	private static final String PD_LINE_REG = "Datfile.*:.*\\(https://.*.zip\\).*";
	private static final String MD_LINK_REG = "\\[(.*)\\].*\\((https:.*.zip)\\)";

    private static final Pattern PD_LINE_PATTERN = Pattern.compile(PD_LINE_REG); 
    private static final Pattern MD_LINK_PATTERN = Pattern.compile(MD_LINK_REG); 
    
    @Override
	public void update() throws Exception {
    	// Latest Mame
    	update("MAME", "https://raw.githubusercontent.com/pleasuredome/pleasuredome/gh-pages/mame/index.md");

    	// Reference Sets
    	update("MAME Reference Sets", "https://raw.githubusercontent.com/pleasuredome/pleasuredome/gh-pages/mame-reference-sets/index.md");

    	// HBMame
    	update("HBMAME", "https://raw.githubusercontent.com/pleasuredome/pleasuredome/gh-pages/hbmame/index.md");

    	// Fruit Machines
    	update("Fruit Machines", "https://raw.githubusercontent.com/pleasuredome/pleasuredome/gh-pages/fruitmachines/index.md");
 	}

	private void update(String name, String url) throws IOException, InterruptedException, URISyntaxException
	{
		final var refRoot = NORMALIZED_DIR.resolve(PD_DIR).resolve(name);
		
		try(final var in = new BufferedReader(new InputStreamReader(HTTP.downloadAsStream(url))))
		{
			if(Files.exists(refRoot))
				Util.deleteRecursive(refRoot);
			Files.createDirectories(refRoot);
			for(final var nf : in.lines().filter(l -> PD_LINE_PATTERN.matcher(l).matches()).map(l -> MD_LINK_PATTERN.matcher(l)).filter(Matcher::find).collect(Collectors.toMap(k -> k.group(1), k -> k.group(2))).entrySet())
			{
				final var encoded = nf.getValue().replace(" ", "%20");
				HTTP.downloadToFolder(URI.create(encoded), refRoot, ZIPMode.SMART);
			}
		}
	}
	
	@Override
	public Path[] getTrackedFolders() {
		return new Path[] { NORMALIZED_DIR.resolve(PD_DIR) };
	}

}
