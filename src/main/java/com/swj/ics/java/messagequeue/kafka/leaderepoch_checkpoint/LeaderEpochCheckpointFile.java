package com.swj.ics.java.messagequeue.kafka.leaderepoch_checkpoint;

import java.io.File;
import java.util.Collection;
import java.util.regex.Pattern;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2024/06/17 17:16
 * this class persists a map of (LeaderEpoch => StartOffset) to a file (for a certain replica)
 * the format in The LeaderEpoch checkpoint file is like this :
 * ------checkpoint file begin------
 * 0        <- LeaderEpochCheckpointFile.version
 * 2        <- the following entries
 * 0 1    <- the format is : leader_epoch(int32) start_offset(int64)
 * 0 2
 * ------checkpoint file end------
 */
public class LeaderEpochCheckpointFile implements LeaderEpochCheckpoint {
  private static final Pattern whiteSpacePattern = Pattern.compile("\\s+");
  private static final String leaderEpochCheckpointFileName = "leader-epoch-checkpoint";
  final Formatter formatter;
  final int currentVersion = 0;
  private final CheckpointFile<EpochEntry> checkpoint;

  public LeaderEpochCheckpointFile(File file) {
    formatter = new Formatter();
    checkpoint = new CheckpointFile<>(file, formatter, currentVersion);
  }

  public File newFile(File dir) {
    return new File(dir, leaderEpochCheckpointFileName);
  }

  @Override
  public void write(Collection<EpochEntry> epochEntries) {
    checkpoint.write(epochEntries);
  }

  @Override
  public Collection<EpochEntry> read() {
    return checkpoint.read();
  }

  class Formatter implements CheckpointFileFormatter<EpochEntry> {

    @Override
    public String toLine(EpochEntry entity) {
      return String.format("%d %d", entity.epoch, entity.startOffset);
    }

    @Override
    public EpochEntry fromLine(String line) {
      if (line == null || line.isEmpty()) {
        return null;
      }
      String[] array = whiteSpacePattern.split(line);
      if (array == null || array.length < 2) {
        return null;
      }
      return new EpochEntry(Integer.parseInt(array[0]), Long.parseLong(array[1]));
    }
  }
}


interface LeaderEpochCheckpoint {

  void write(Collection<EpochEntry> epochEntries);

  Collection<EpochEntry> read();
}
