package io.github.makbn.jthumbnail.core.office;

import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.core.task.OfficeTask;

/**
 * No-op {@link OfficeManager} for tests or when LibreOffice is not available.
 * Start/stop are no-ops; {@link #execute(OfficeTask)} throws
 * {@link UnsupportedOperationException}.
 */
public final class NoOpOfficeManager implements OfficeManager {

    @Override
    public void start() throws OfficeException {
        // no-op
    }

    @Override
    public void stop() throws OfficeException {
        // no-op
    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public void execute(OfficeTask task) throws OfficeException {
        throw new UnsupportedOperationException("NoOpOfficeManager: LibreOffice is not configured");
    }
}
