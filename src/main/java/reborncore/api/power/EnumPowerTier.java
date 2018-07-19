/*
 * Copyright (c) 2018 modmuss50 and Gigabit101
 *
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 *
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package reborncore.api.power;

/**
 * Created by modmuss50 on 08/03/2016.
 */
public enum EnumPowerTier {
	MICRO(8, 8),
	LOW(32, 32),
	MEDIUM(128, 128),
	HIGH(512, 512),
	EXTREME(2048, 2048),
	INSANE(8192, 8192),
	INFINITE(Integer.MAX_VALUE, Integer.MAX_VALUE);

	private final int maxInput;
	private final int maxOutput;

	EnumPowerTier(int maxInput, int maxOutput) {
		this.maxInput = maxInput;
		this.maxOutput = maxOutput;
	}

	public int getMaxInput() {
		return maxInput;
	}

	public int getMaxOutput() {
		return maxOutput;
	}

	public static EnumPowerTier getTeir(int power) {
		for (EnumPowerTier tier : EnumPowerTier.values()) {
			if (tier.maxInput >= power) {
				return tier;
			}
		}
		return EnumPowerTier.INFINITE;
	}
}
