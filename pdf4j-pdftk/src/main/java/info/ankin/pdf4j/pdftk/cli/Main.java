package info.ankin.pdf4j.pdftk.cli;

import lombok.Data;
import picocli.CommandLine;

@Data
@CommandLine.Command(
        name = "pdftk4j",
        description = "pdftk APL port",
        version = "0.0.1",
        mixinStandardHelpOptions = true,
        sortOptions = false,
        sortSynopsis = false,
        subcommands = {}
)
public class Main implements Runnable {
    @CommandLine.Option(names = "dont_ask")
    boolean dont_ask;

    @CommandLine.Option(names = "do_ask")
    boolean do_ask;

    @CommandLine.Option(names = "debug")
    boolean debug;

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    public static void main(String[] args) {
        System.exit(new CommandLine(new Main()).execute(args));
    }

    @Override
    public void run() {
        if (debug) {
            System.out.println(this);
            return;
        }
        spec.commandLine().usage(System.err);
    }
}
