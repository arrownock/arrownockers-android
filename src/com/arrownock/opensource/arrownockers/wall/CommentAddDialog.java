package com.arrownock.opensource.arrownockers.wall;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.arrownock.opensource.arrownockers.R;

public class CommentAddDialog extends Dialog {
	Context context;
	String parentId = null;
	Button btn_cancel;
	Button btn_send;
	RadioGroup radioGroup;
	EditText et_content;

	public CommentAddDialog(Context context) {
		super(context);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.context = context;
		Window window = this.getWindow();
		window.setContentView(R.layout.dialog_comment_add);

		btn_cancel = (Button) window.findViewById(R.id.btn_cancel);
		btn_send = (Button) window.findViewById(R.id.btn_send);
		radioGroup = (RadioGroup) window.findViewById(R.id.radioGroup);
		et_content = (EditText) window.findViewById(R.id.et_content);
	}

	public void setPositiveButton(final View.OnClickListener listener) {
		btn_send.setOnClickListener(listener);
	}

	public void setNegativeButton(final View.OnClickListener listener) {
		btn_cancel.setOnClickListener(listener);
	}

	public Boolean getLike() {
		Boolean positive = false;

		int checkedId = radioGroup.getCheckedRadioButtonId();
		if (checkedId == R.id.rbtn_flower) {
			positive = true;
		}

		return positive;
	}

	public String getContent() {
		String content = null;

		if (et_content.getText() != null)
			content = et_content.getText().toString();

		return content;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

}