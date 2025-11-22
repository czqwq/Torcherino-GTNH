package com.sci.torcherino.update;

/**
 * @author sci4me
 * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
 */
public final class ModVersion {
    private static final ModVersion NULL_VERSION = new ModVersion(0, 0);

    private int major;
    private int minor;

    public ModVersion(final int major, final int minor) {
        this.major = major;
        this.minor = minor;
    }

    public static ModVersion parse(final String version) {
        try {
            final String[] parts = version.split("\\.");

            return new ModVersion(
                    Integer.valueOf(parts[0]),
                    Integer.valueOf(parts[1].substring(0, parts[1].length() - 1))
            );
        } catch (Throwable t) {
            return NULL_VERSION;
        }
    }

    @Override
    public String toString() {
        return this.major + "." + this.minor + "s";
    }

    public boolean isNewer(final ModVersion newer) {
        if (newer.major > this.major) {
            return true;
        } else if (newer.minor > this.minor) {
            return true;
        }
        return false;
    }
}