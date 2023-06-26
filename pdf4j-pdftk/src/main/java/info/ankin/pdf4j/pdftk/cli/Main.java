package info.ankin.pdf4j.pdftk.cli;

import lombok.Data;
import lombok.ToString;
import picocli.AutoComplete;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@CommandLine.Command(
        name = "pdftk4j",
        description = "pdftk APL port",
        version = "0.0.1",
        mixinStandardHelpOptions = true,
        sortOptions = false,
        sortSynopsis = false,
        usageHelpAutoWidth = true,
        subcommands = {
                Main.Completion.class,
                Main.CatCommand.class,
                Main.ShuffleCommand.class,
                Main.BurstCommand.class,
                Main.RotateCommand.class,
                Main.GenFdfCommand.class,
                Main.FillFormCommand.class,
                Main.BackgroundCommand.class,
                Main.DumpDataCommand.class,
                Main.UpdateInfoCommand.class,
                Main.AttachFilesCommand.class,
                Main.UnpackFilesCommand.class,
                Main.AllowCommand.class,
                Main.OwnerPasswordCommand.class,
                Main.UserPasswordCommand.class,
                Main.CompressCommand.class,
                Main.UncompressCommand.class,
                Main.FlattenCommand.class,
        }
)
public class Main implements Runnable {
    @ToString.Exclude
    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    @CommandLine.Mixin
    StandardOptions standardOptions;

    @CommandLine.Option(
            names = "--encrypt",
            arity = "0..1",
            paramLabel = "strength",
            completionCandidates = EncryptionCompletion.class,
            preprocessor = EncryptionPreProcessor.class,
            fallbackValue = "aes128",
            scope = CommandLine.ScopeType.INHERIT
    )
    Encryption encrypt;

    @CommandLine.Option(names = {"-f", "--flat", "--flatten"}, description = "flatten the output", scope = CommandLine.ScopeType.INHERIT)
    boolean flatten;

    @CommandLine.Option(names = {"--need-appearances"},
            description = "Sets a flag that cues Reader/Acrobat to " +
                          "generate new field appearances based on the form field values.",
            scope = CommandLine.ScopeType.INHERIT)
    boolean needAppearances;

    @CommandLine.Mixin
    InputPasswordMixin inputPassword;

    @CommandLine.Option(names = "--dont_ask")
    boolean dontAsk;

    @CommandLine.Option(names = "--do_ask", description = "takes priority over --dont_ask")
    boolean doAsk;

    @CommandLine.Option(names = "-v",
            description = {
                    "Specify multiple -v options to increase verbosity.",
                    "For example, `-v -v -v` or `-vvv`"
            })
    boolean[] verbosity = {};

    public static void main(String[] args) {
        System.exit(new CommandLine(new Main()).execute(args));
    }

    @Override
    public void run() {
        if (isVerbose()) {
            System.out.println(this);
            return;
        }
        spec.commandLine().usage(System.err);
    }

    private StandardOptions combineWith(StandardOptions otherStdOptions) {
        StandardOptions combined = new StandardOptions();

        ArrayList<String> inputs = new ArrayList<>();
        if (notEmpty(standardOptions.getInputs())) inputs.addAll(standardOptions.getInputs());
        if (notEmpty(otherStdOptions.getInputs())) inputs.addAll(otherStdOptions.getInputs());
        combined.setInputs(inputs);

        ArrayList<String> outputs = new ArrayList<>();
        if (notEmpty(standardOptions.getOutputs())) outputs.addAll(standardOptions.getOutputs());
        if (notEmpty(otherStdOptions.getOutputs())) outputs.addAll(otherStdOptions.getOutputs());
        combined.setOutputs(outputs);

        return combined;
    }

    boolean notEmpty(List<?> list) {
        return list != null && !list.isEmpty();
    }

    enum Encryption {
        _40bit,
        _128bit,
        aes128,
        ;

        static final Map<String, Encryption> map = Stream.of(values())
                .collect(Collectors.toMap(Enum::name, Function.identity()));

        static Encryption of(String value) {
            return map.getOrDefault(value, map.get("_" + value));
        }
    }

    static class EncryptionCompletion implements Iterable<String> {
        @Override
        public Iterator<String> iterator() {
            return List.of("40bit", "128bit", "aes128").iterator();
        }
    }

    static class EncryptionPreProcessor implements CommandLine.IParameterPreprocessor {
        @Override
        public boolean preprocess(Stack<String> args,
                                  CommandLine.Model.CommandSpec commandSpec,
                                  CommandLine.Model.ArgSpec argSpec,
                                  Map<String, Object> info) {
            if (args.isEmpty()) return false;
            Encryption e = Encryption.of(args.peek());
            if (e != null) {
                args.pop();
                args.push(e.name());
            }
            return false;
        }
    }

    @CommandLine.Command(
            name = "completion",
            description = "Generate shell completion script for ${ROOT-COMMAND-NAME:-<command>}.",
            subcommands = {Completion.BashCompletion.class},
            mixinStandardHelpOptions = true)
    static class Completion {
        @CommandLine.Command(name = "bash", aliases = {"zsh"})
        static class BashCompletion extends AutoComplete.GenerateCompletion {
        }
    }

    @ToString
    public static abstract class AbstractSubCommand implements Runnable {
        @ToString.Exclude
        @CommandLine.ParentCommand
        Main main;

        @CommandLine.Mixin
        StandardOptions standardOptions;

        @Override
        public void run() {
            standardOptions = main.combineWith(standardOptions);
            if (main.isVerbose())
               System.out.println(this);
            else System.err.println("Not Implemented Yet");
        }

        @ToString.Include
        public String type() {
            return getClass().getSimpleName();
        }
    }

    private boolean isVerbose() {
        return verbosity.length > 0;
    }

    @Data
    static class StandardOptions {
        @CommandLine.Option(names = {"-i", "--input"}, arity = "1..")
        List<String> inputs;

        @CommandLine.Option(names = {"-o", "--output"}, arity = "1..")
        List<String> outputs;
    }

    @ToString
    public static abstract class AbstractVarArgsSubCommand extends AbstractSubCommand {
        @CommandLine.Parameters(arity = "1..")
        List<String> arguments;
    }

    @ToString
    public static abstract class AbstractVarFilesSubCommand extends AbstractSubCommand {
        @CommandLine.Parameters(arity = "1..")
        List<Path> files;
    }

    @ToString
    public static abstract class AbstractFileArgSubCommand extends AbstractSubCommand {
        @CommandLine.Parameters(index = "0")
        Path file;
    }

    @ToString(callSuper = true)
    @CommandLine.Command(
            name = "cat",
            description = "concatenates pages from input pdfs",
            mixinStandardHelpOptions = true)
    public static class CatCommand extends AbstractVarArgsSubCommand {
    }

    @ToString(callSuper = true)
    @CommandLine.Command(
            name = "shuffle",
            description = "Collates pages from input PDFs to create a new PDF.",
            mixinStandardHelpOptions = true)
    public static class ShuffleCommand extends AbstractVarArgsSubCommand {
    }

    @ToString(callSuper = true)
    @CommandLine.Command(
            name = "burst",
            description = "Splits a single input PDF document into individual pages.",
            mixinStandardHelpOptions = true)
    public static class BurstCommand extends AbstractSubCommand {
    }

    @ToString(callSuper = true)
    @CommandLine.Command(
            name = "rotate",
            description = "Takes a single input PDF and rotates just the specified pages.",
            mixinStandardHelpOptions = true)
    public static class RotateCommand extends AbstractVarArgsSubCommand {
    }

    @ToString(callSuper = true)
    @CommandLine.Command(
            name = "generate_fdf",
            description = "Generates a file suitable for fill_form subcommand",
            mixinStandardHelpOptions = true)
    public static class GenFdfCommand extends AbstractFileArgSubCommand {
    }

    /**
     * consider a new base or special case for parsing args
     */
    @ToString(callSuper = true)
    @CommandLine.Command(
            name = "fill_form",
            description = "Fills the single input PDF file's form fields with the data from an FDF file, XFDF file or stdin",
            mixinStandardHelpOptions = true)
    public static class FillFormCommand extends AbstractFileArgSubCommand {
    }

    @ToString(callSuper = true)
    @CommandLine.Command(
            name = "background",
            description = "Applies a PDF watermark to the background of input files",
            mixinStandardHelpOptions = true)
    public static class BackgroundCommand extends AbstractFileArgSubCommand {
        @CommandLine.Option(names = {"-M", "--multi"}, description = "Use every page of the background file, not just the first")
        boolean multi;

        @CommandLine.Option(names = {"-S", "--stamp"}, description = "Use the background file as a foreground file (like a stamp)")
        boolean stamp;
    }

    @ToString(callSuper = true)
    @CommandLine.Command(name = "dump_data",
            description = "Reads a single input PDF file and displays metadata: " +
                          "bookmarks (a/k/a outlines), " +
                          "page metrics (media, rotation and labels), " +
                          "data embedded by STAMPtk (see STAMPtk embed option) " +
                          "and other data to the given output filename " +
                          "or (if no output is given) to stdout. " +
                          "Non-ASCII characters are encoded as XML numerical entities. " +
                          "Does not create a new PDF.",
            mixinStandardHelpOptions = true)
    public static class DumpDataCommand extends AbstractFileArgSubCommand {
        @CommandLine.Option(names = {"-U", "--utf8"}, description = "the output data is encoded as UTF-8.")
        boolean unicode;

        @CommandLine.Option(names = {"-A", "--annots", "--annotations"},
                description = "Print only links - mutually exclusive with fields (annotations takes precedence)")
        boolean annotations;

        @CommandLine.Option(names = {"--fields"}, description = "Report statistics instead of field values")
        boolean fields;
    }

    @ToString(callSuper = true)
    @CommandLine.Command(
            name = "update_info",
            description = "Changes what dump_data outputs, on the given file",
            mixinStandardHelpOptions = true)
    public static class UpdateInfoCommand extends AbstractFileArgSubCommand {
        @CommandLine.Option(names = {"-U", "--utf8"}, description = "the input data is encoded as UTF-8.")
        boolean unicode;
    }

    @ToString(callSuper = true)
    @CommandLine.Command(
            name = "attach_files",
            description = "Packs arbitrary files into a PDF using PDF's file attachment features.",
            mixinStandardHelpOptions = true)
    public static class AttachFilesCommand extends AbstractVarFilesSubCommand {
    }

    @ToString(callSuper = true)
    @CommandLine.Command(
            name = "unpack_files",
            description = "Unpacks attached files from a PDF into a folder",
            mixinStandardHelpOptions = true)
    public static class UnpackFilesCommand extends AbstractFileArgSubCommand {
    }

    @ToString(callSuper = true)
    @CommandLine.Command(
            name = "allow",
            description = {"Permissions are applied to the output PDF only if an encryption",
                    "strength is specified or an owner or user password is given.",
                    "If permissions are not specified, all permissions are removed."},
            mixinStandardHelpOptions = true)
    public static class AllowCommand extends AbstractSubCommand {
        @CommandLine.Parameters(arity = "0..")
        List<SupportedPermission> permissions;
    }

    @Data
    @CommandLine.Command(sortOptions = false, sortSynopsis = false)
    static class PasswordMixin {
        @CommandLine.Option(
                names = {"-p:e", "--password:env"},
                interactive = true,
                description = {
                        "environment variable from which to read password value",
                        "(takes precedence over file and value)"
                })
        String passwordEnvVar;
        @CommandLine.Option(
                names = {"-p:f", "--password:file"},
                interactive = true,
                description = {
                        "file from which to read password value",
                        "(takes precedence over value)"
                })
        Path passwordFile;
        @CommandLine.Option(
                names = {"-p", "--password"},
                interactive = true,
                description = "password value (last priority - consider using file or env var)")
        String password;
    }

    @Data
    @CommandLine.Command(sortOptions = false, sortSynopsis = false)
    static class InputPasswordMixin {
        @CommandLine.Option(
                names = {"-ip:e", "--input-password:env"},
                interactive = true,
                description = {
                        "environment variable from which to read password value",
                        "(takes precedence over file and value)"
                })
        String passwordEnvVar;
        @CommandLine.Option(
                names = {"-ip:f", "--input-password:file"},
                interactive = true,
                description = {
                        "file from which to read password value",
                        "(takes precedence over value)"
                })
        Path passwordFile;
        @CommandLine.Option(
                names = {"-ip", "--input-password"},
                interactive = true,
                description = "password value (last priority - consider using file or env var)")
        String password;
    }

    @ToString(callSuper = true)
    @CommandLine.Command(
            name = "owner_pw",
            aliases = "owner_password",
            description = {
                    "Permissions are applied to the output PDF only if an encryption",
                    "strength is specified or an owner or user password is given.",
                    "If permissions are not specified, all permissions are removed."
            },
            mixinStandardHelpOptions = true)
    public static class OwnerPasswordCommand extends AbstractSubCommand {
        @ToString.Exclude
        @CommandLine.ParentCommand
        Main main;

        @CommandLine.Mixin
        PasswordMixin password;

        @ToString.Include
        public Encryption encryption() {
            return main.getEncrypt();
        }
    }

    @ToString(callSuper = true)
    @CommandLine.Command(
            name = "user_pw",
            aliases = "user_password",
            description = {
                    "Permissions are applied to the output PDF only if an encryption",
                    "strength is specified or an owner or user password is given.",
                    "If permissions are not specified, all permissions are removed."
            },
            mixinStandardHelpOptions = true)
    public static class UserPasswordCommand extends AbstractSubCommand {
        @ToString.Exclude
        @CommandLine.ParentCommand
        Main main;

        @CommandLine.Mixin
        PasswordMixin password;

        @ToString.Include
        public Encryption encryption() {
            return main.getEncrypt();
        }
    }

    @ToString(callSuper = true)
    @CommandLine.Command(name = "compress", description = "compress the file", mixinStandardHelpOptions = true)
    public static class CompressCommand extends AbstractSubCommand {
    }

    @ToString(callSuper = true)
    @CommandLine.Command(name = "uncompress", description = "compress the file", mixinStandardHelpOptions = true)
    public static class UncompressCommand extends AbstractSubCommand {
    }

    @ToString(callSuper = true)
    @CommandLine.Command(name = "flatten", description = "like --flatten, but when its the only action to take", mixinStandardHelpOptions = true)
    public static class FlattenCommand extends AbstractSubCommand {
    }

    /*
        todo:
        [replacement_font <font name>]
        [keep_first_id | keep_final_id]
        [drop_xfa]
        [drop_xmp]
     */
}
