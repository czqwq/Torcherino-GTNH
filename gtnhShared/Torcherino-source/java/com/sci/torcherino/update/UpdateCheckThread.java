package com.sci.torcherino.update;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * @author sci4me
 * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
 */
class UpdateCheckThread extends Thread {
    private IUpdatableMod mod;

    private boolean checkComplete = false;
    private boolean newVer = false;
    private ModVersion latest;
    private String description;

    public UpdateCheckThread(final IUpdatableMod mod) {
        super("TorcherinoUpdater: " + mod.name());

        this.mod = mod;
        this.setDaemon(true);
    }

    @Override
    public void run() {
        int attempt = 0;
        while (attempt < 5) {
            attempt++;

            try {
                final URL versionUrl = new URL(this.mod.updateURL());

                final StringBuilder sb = new StringBuilder();
                final BufferedReader reader = new BufferedReader(new InputStreamReader(versionUrl.openStream()));
                String line = reader.readLine();
                while (true) {
                    sb.append(line);

                    line = reader.readLine();
                    if (line == null) break;
                    else sb.append('\n');
                }
                reader.close();

                final UpdateData data = UpdateData.parse(sb.toString());

                if (data.latest != null) {
                    this.latest = data.latest;
                    this.description = data.description;
                    this.newVer = this.mod.version().isNewer(data.latest);
                    this.checkComplete = true;

                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
        this.checkComplete = true;
    }

    public boolean checkComplete() {
        return this.checkComplete;
    }

    public boolean newVersionAvailable() {
        return this.newVer;
    }

    public ModVersion latest() {
        return this.latest;
    }

    public String description() {
        return this.description;
    }
}