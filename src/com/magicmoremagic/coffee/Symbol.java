package com.magicmoremagic.coffee;

import java.util.Random;

public interface Symbol {

	Symbol clone();
	void brew(Random rnd, StringBuilder dest);
}
