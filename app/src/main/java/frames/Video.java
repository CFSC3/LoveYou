package frames;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.VideoView;

import com.example.a0411.R;

public class Video extends Fragment {

    private VideoView video;
    private Musicas musicas;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_video, container, false);

        video = view.findViewById(R.id.videoView);
        String videoPath = "android.resource://" + getContext().getPackageName() + "/" + R.raw.coracao;
        Uri uri = Uri.parse(videoPath);
        video.setVideoURI(uri);

        video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.setLooping(true);
            }
        });

        video.start();

        video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                video.pause();
                reproducao();
            }
        });

        return view;

    }

    public void reproducao(){

        musicas = new Musicas();
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.passeDeFrames, musicas );
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}