package com.hitstreamr.hitstreamrbeta.DrawerMenuFragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.hitstreamr.hitstreamrbeta.MainActivity;
import com.hitstreamr.hitstreamrbeta.R;

public class LegalAgreementsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_legalagreements, container, false);


        Button tos = (Button) view.findViewById(R.id.tos_button);
        Button pp = (Button) view.findViewById(R.id.pp_button);
        Button close = (Button) view.findViewById(R.id.closeBtn);

        tos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.hitstreamr.com/terms-of-service"));
                startActivity(browserIntent);
            }
        });

        pp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.hitstreamr.com/privacy-policy"));
                startActivity(browserIntent);
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.putExtra("TYPE", getArguments().getString("TYPE"));
                startActivity(intent);
            }
        });

        return view;


    }
}
