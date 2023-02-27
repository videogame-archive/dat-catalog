package com.github.openretrogamingarchive;

import com.github.openretrogamingarchive.updaters.*;

public class Main {

	public static void main(String[] args) throws Exception {
		Redump.update();
		TOSEC.update();
		NoIntro.update();
		Indexes.update();
	}
}