package net.sourceforge.eclipseccase.ui;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class CheckinDialog extends InputDialog
{
	Button recursiveButton;
	boolean recursive = false;
	
	/**
	 * Constructor for CheckinDialog.
	 * @param parentShell
	 * @param dialogTitle
	 * @param dialogMessage
	 * @param initialValue
	 * @param validator
	 */
	public CheckinDialog(
		Shell parentShell,
		String dialogTitle,
		String dialogMessage,
		String initialValue,
		IInputValidator validator)
	{
		super(parentShell, dialogTitle, dialogMessage, initialValue, validator);
	}

	/**
	 * @see Dialog#createButtonsForButtonBar(Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent)
	{
		super.createButtonsForButtonBar(parent);
	}

	/**
	 * Gets the recursive.
	 * @return Returns a boolean
	 */
	public boolean isRecursive()
	{
		return recursive;
	}

	/**
	 * Sets the recursive.
	 * @param recursive The recursive to set
	 */
	public void setRecursive(boolean recursive)
	{
		this.recursive = recursive;
	}

	/**
	 * @see Dialog#createDialogArea(Composite)
	 */
	protected Control createDialogArea(Composite parent)
	{
		Composite control = (Composite) super.createDialogArea(parent);
		recursiveButton = new Button(control, SWT.CHECK);
		recursiveButton.setText("Recurse");
		return control;
	}

	/**
	 * @see Dialog#buttonPressed(int)
	 */
	protected void buttonPressed(int buttonId)
	{
		if (buttonId == IDialogConstants.OK_ID)
		{
			recursive = recursiveButton.getSelection();
		}
		super.buttonPressed(buttonId);
	}
}