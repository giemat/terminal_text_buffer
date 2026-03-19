# Terminal Text Buffer

Small Java terminal-buffer project with an interactive CLI demo.

## Run

```bash
mvn -q -DskipTests package
java -jar target/terminal_text_buffer-1.0-SNAPSHOT.jar
```

At startup, the app asks for:
- `width`
- `height`
- `max scrollback`

Then it shows a small terminal window and accepts commands.

## Interactive Commands

- `write <text>`: write text at current cursor
- `insert <text>`: insert text at current cursor
- `cursor <col> <row>`: move cursor (clamped)
- `fill <row> <char>`: fill a row with one character
- `scroll`: move top line to scrollback, append blank line
- `clear`: clear screen
- `clearall`: clear screen and scrollback
- `show`: redraw
- `help`: print command list
- `exit` or `quit`: quit

Tip: any unrecognized input is treated as plain `write` text.

