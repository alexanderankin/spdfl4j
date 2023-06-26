# pdftk4j

supporting the argument format of the original pdftk utility is in scope,
but to simplify initial development, standard get_opt style options are implemented first.

```
Usage: pdftk4j [-fvhV] [-i=<inputs>...]... [-o=<outputs>...]... [--encrypt
               [=strength]] [--need-appearances] [-ip:e] [-ip:f] [-ip]
               [--dont_ask] [--do_ask] [COMMAND]
pdftk APL port
  -i, --input=<inputs>...
  -o, --output=<outputs>...
      --encrypt[=strength]
  -f, --flat, --flatten      flatten the output
      --need-appearances     Sets a flag that cues Reader/Acrobat to generate
                               new field appearances based on the form field
                               values.
      -ip:e, --input-password:env
                             environment variable from which to read password
                               value
                             (takes precedence over file and value)
      -ip:f, --input-password:file
                             file from which to read password value
                             (takes precedence over value)
      -ip, --input-password  password value (last priority - consider using
                               file or env var)
      --dont_ask
      --do_ask               takes priority over --dont_ask
  -v                         Specify multiple -v options to increase verbosity.
                             For example, `-v -v -v` or `-vvv`
  -h, --help                 Show this help message and exit.
  -V, --version              Print version information and exit.
Commands:
  completion                Generate shell completion script for pdftk4j.
  cat                       concatenates pages from input pdfs
  shuffle                   Collates pages from input PDFs to create a new PDF.
  burst                     Splits a single input PDF document into individual
                              pages.
  rotate                    Takes a single input PDF and rotates just the
                              specified pages.
  generate_fdf              Generates a file suitable for fill_form subcommand
  fill_form                 Fills the single input PDF file's form fields with
                              the data from an FDF file, XFDF file or stdin
  background                Applies a PDF watermark to the background of input
                              files
  dump_data                 Reads a single input PDF file and displays
                              metadata: bookmarks (a/k/a outlines), page
                              metrics (media, rotation and labels), data
                              embedded by STAMPtk (see STAMPtk embed option)
                              and other data to the given output filename or
                              (if no output is given) to stdout. Non-ASCII
                              characters are encoded as XML numerical entities.
                              Does not create a new PDF.
  update_info               Changes what dump_data outputs, on the given file
  attach_files              Packs arbitrary files into a PDF using PDF's file
                              attachment features.
  unpack_files              Unpacks attached files from a PDF into a folder
  allow                     Permissions are applied to the output PDF only if
                              an encryption
  owner_pw, owner_password  Permissions are applied to the output PDF only if
                              an encryption
  user_pw, user_password    Permissions are applied to the output PDF only if
                              an encryption
  compress                  compress the file
  uncompress                compress the file
  flatten                   like --flatten, but when its the only action to take
```
