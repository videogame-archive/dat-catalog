package com.github.openretrogamingarchive;

import java.nio.file.Files;
import java.nio.file.Path;

import com.github.openretrogamingarchive.helpers.Modified;
import com.github.openretrogamingarchive.updaters.NoIntro;
import com.github.openretrogamingarchive.updaters.Redump;
import com.github.openretrogamingarchive.updaters.TOSEC;
import com.github.openretrogamingarchive.updaters.Updater;

public class Main {

	public static void main(String[] args) throws Exception {
		Updater[] updaters = new Updater[] { new NoIntro(), new Redump(), new TOSEC()};
		for (Updater updater: updaters) {
			try {
				System.out.println("Initializing " + updater.getClass().getSimpleName());
				if (shouldUpdate(updater)) {
					System.out.println("Updating " + updater.getClass().getSimpleName());
					updater.update();
					for (Path trackedFolder : updater.getTrackedFolders()) {
						System.out.println("Updating modified for " + trackedFolder);
						Modified.setModified(trackedFolder);
						System.out.println("Updating index.csv for " + trackedFolder);
						Indexes.update(trackedFolder, true);
					}
				} else {
					System.out.println("Up to date " + updater.getClass().getSimpleName());
				}
			} catch (Exception ex) {
				System.out.println("Failed to update: " + updater.getClass().getSimpleName() + " with error: " + ex.getMessage());
			}
		}
	}

	private static boolean shouldUpdate(Updater updater) {
		boolean trackedExists = true;
		for (Path trackedFolder : updater.getTrackedFolders()) {
			trackedExists = trackedExists && Files.exists(trackedFolder);
		}
		if (!trackedExists) {
			return true;
		}
		boolean trackedOld = false;
		for (Path trackedFolder : updater.getTrackedFolders()) {
			trackedOld = trackedOld || Modified.isOneDayOld(trackedFolder);
		}
		return trackedOld;
	}

}