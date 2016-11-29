package me.coley.gui.listener;

import java.util.Map.Entry;

import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import io.github.bmf.util.mapping.ClassMapping;
import me.coley.LineContext;
import me.coley.Program;
import me.coley.gui.component.JavaTextArea;
import me.coley.util.StringUtil;

public class JavaCaretListener implements CaretListener {
	private final Program callback;
	private final JavaTextArea text;
	private int selectedLine;
	private LineContext lineContext;
	private String lineContent;
	private String word;
	private ClassMapping selectedMapping;

	public JavaCaretListener(Program callback, JavaTextArea text) {
		this.callback = callback;
		this.text = text;
	}

	@Override
	public void caretUpdate(CaretEvent e) {
		if (!text.canParse()) {
			return;
		}
		int dot = e.getDot();
		if (dot == 0 || dot >= text.getText().length() - 1) {
			return;
		}
		// Getting the line data
		int lineEnd = text.getText().substring(dot).indexOf("\n");
		String firstPart = text.getText().substring(0, dot + lineEnd);
		int lineStart = firstPart.lastIndexOf("\n") + 1;
		String line = firstPart.substring(lineStart, firstPart.length());
		String word = StringUtil.getWordAtIndex(dot - lineStart, line);
		// Parsing the line data
		this.selectedLine = firstPart.split("\n").length - 1;
		this.lineContext = text.getContext(selectedLine);
		this.lineContent = line;
		if (word.length() > 0) {
			this.word = word;
			this.selectedMapping = detectMapping();
			// Enable interaction
			text.setEditable(true);
		} else {
			this.word = null;
			this.selectedMapping = null;
			// Disable interaction
			text.setEditable(false);
		}
	}

	/**
	 * Detects a class based on the current selection.
	 * 
	 * @return
	 */
	private ClassMapping detectMapping() {
		for (Entry<String, ClassMapping> entry : callback.getJarReader().getMapping().getMappings().entrySet()) {
			ClassMapping cm = entry.getValue();
			String nameOriginal = entry.getKey();
			String nameCurrent = cm.name.getValue();
			String cutOrig = nameOriginal.substring(nameOriginal.lastIndexOf("/") + 1);
			String cutCurr = nameCurrent.substring(nameCurrent.lastIndexOf("/") + 1);
			if (word.equals(cutOrig) || word.equals(cutCurr)) {
				return cm;
			}
		}
		return null;
	}

	/**
	 * Returns the current line based on the caret position.
	 * 
	 * @return
	 */
	public int getSelectedLine() {
		return selectedLine;
	}

	/**
	 * Returns the current line ({@linkplain #getSelectedLine()}) context.
	 * 
	 * @return
	 */
	public LineContext getLineContext() {
		return lineContext;
	}

	/**
	 * Returns the text of the current line ({@linkplain #getSelectedLine()}).
	 * 
	 * @return
	 */
	public String getLineContent() {
		return lineContent;
	}

	/**
	 * Returns the selected word in the current line (
	 * {@linkplain #getLineContent()}).
	 * 
	 * @return Null if selection is invalid. Single word otherwise.
	 */
	public String getWord() {
		return word;
	}

	/**
	 * Returns the ClassMapping based on the current selection (
	 * {@linkplain #getWord()}).
	 * 
	 * @return
	 */
	public ClassMapping getSelectedMapping() {
		return selectedMapping;
	}
}