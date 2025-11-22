package com.sci.torcherino.update;

/**
 * @author sci4me
 * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
 */
public interface IUpdatableMod
{
    String name();

    String updateURL();

    ModVersion version();
}