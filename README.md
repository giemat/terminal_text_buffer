# Terminal Text Buffer

Small Java terminal-buffer project with an interactive CLI demo.

## Run

```bash
./run-app.sh

# or manually:
mvn -q -DskipTests package
java -jar target/terminal_text_buffer-1.0-SNAPSHOT.jar
```

At startup, the app asks for:
- `width`
- `height`
- `max scrollback`
- `foreground color`
- `background color`
- `styles` (comma-separated, or `none`)

Then it shows a small terminal window and accepts commands.

## Interactive Commands

- `write <text>`: write text at current cursor
- `insert <text>`: insert text at current cursor
- `cursor <col> <row>`: move cursor (clamped)
- `h [n]`: move cursor right by `n` cells (default `1`)
- `j [n]`: move cursor down by `n` cells (default `1`)
- `k [n]`: move cursor up by `n` cells (default `1`)
- `l [n]`: move cursor left by `n` cells (default `1`)
- `style add <list>`: add styles (comma-separated, e.g. `bold,underline`)
- `style rm <list>`: remove styles (comma-separated)
- `style clear`: remove all active styles
- `style show`: print active styles
- `fg <color>`: set active foreground color (e.g. `fg red`, `fg default`)
- `bg <color>`: set active background color (e.g. `bg blue`, `bg default`)
- `attr show`: print active attributes (foreground, background, styles)
- `attr reset`: reset active attributes to defaults
- `getch <col> <row>`: print character at position
- `getattr <col> <row>`: print attributes at position
- `getline <row>`: print line text for row
- `getscreen`: print full visible screen content
- `getall`: print full scrollback + screen content
- `fill <row> <char>`: fill a row with one character
- `scroll`: move top line to scrollback, append blank line
- `clear`: clear screen
- `clearall`: clear screen and scrollback
- `show`: redraw
- `help`: print command list
- `exit` or `quit`: quit

Tip: any unrecognized input is treated as plain `write` text.

Row indexing for query commands:
- screen rows: `0..height-1`
- scrollback rows: negative values (`-1` is newest scrollback line)

## Scrollback And View APIs

- `getChar(col, row)`, `getAttributes(col, row)`, and `getLineAsString(row)` support:
  - screen rows: `0..height-1`
  - scrollback rows: negative values, where `-1` is the newest scrollback line
- Scrollback lines are archived copies; screen edits do not mutate existing scrollback entries.
- Optional viewport helpers:
  - `setViewOffset(linesUp)` shifts a virtual view into scrollback (clamped to available lines)
  - `getViewOffset()` returns the current offset
  - `getVisibleLineAsString(viewRow)` reads by view row using current offset

