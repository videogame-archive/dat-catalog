package com.github.openretrogamingarchive;

import java.nio.file.Path;

import com.github.openretrogamingarchive.helpers.Modified;
import com.github.openretrogamingarchive.updaters.NoIntro;
import com.github.openretrogamingarchive.updaters.Redump;
import com.github.openretrogamingarchive.updaters.TOSEC;
import com.github.openretrogamingarchive.updaters.Updater;

public class Main {

	public static void main(String[] args) throws Exception {
		Updater[] updaters = new Updater[] { new Redump(), new TOSEC(), new NoIntro()};
		for (Updater updater: updaters) {
			updater.update();
			for (Path trackedFolder:updater.getTrackedFolders()) {
				Modified.setModified(trackedFolder);
				Indexes.update(trackedFolder, true);
			}
		}
	}
}