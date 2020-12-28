package com.example.desktoptext;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class AlertDialogFragment extends DialogFragment {
    public interface AlertDialogListener {
        public void showDialogOverrideSlot(DialogFragment dialog, int selectedPosition);
    }

    // Use this instance of the interface to deliver action events
    AlertDialogListener listener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = (AlertDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement AlertDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstance) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        // Set the dialog title
        builder.setTitle("所有栏位已满，请选择需要覆盖的栏位~")
                // Specify the list array, the items to be selected by default (null for none),
                // and the listener through which to receive callbacks when items are selected
                .setSingleChoiceItems(R.array.plan_array, 0, null)

                .setPositiveButton(this.getString(R.string.confirm_override), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
//                        dialog.dismiss();
                        int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
//                        System.out.println(selectedPosition);
                        listener.showDialogOverrideSlot(AlertDialogFragment.this, selectedPosition);
                    }
                })
                .setNegativeButton(this.getString(R.string.cancle), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });

        return builder.create();
    }

}
