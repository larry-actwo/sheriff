package net.bitcoinguard.sheriff.bitcoin.configuration;

import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.core.NetworkParameters;
import com.google.bitcoin.core.Wallet;
import com.google.bitcoin.kits.WalletAppKit;
import com.google.bitcoin.params.TestNet3Params;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

/**
 * Created by Jiri on 21. 7. 2014.
 */
@Configuration
public class WalletConfiguration {


    @Value("${bitcoin.wallet.name}")
    private String filePrefix;
    @Value("${bitcoin.wallet.directory}")
    private String walletDirectory;
    @Value("${bitcoin.wallet.network}")
    private String network;

    @Bean
    Wallet wallet(){
        WalletAppKit walletAppKit = new WalletAppKit(networkParameters(), workingDirectory(), filePrefix) {
            @Override
            protected void onSetupCompleted() {
                // This is called in a background thread after startAndWait is called, as setting up various objects
                // can do disk and network IO that may cause UI jank/stuttering in wallet apps if it were to be done
                // on the main thread.
                while ((wallet().getImportedKeys().size() < 1)) {
                    wallet().importKey(new ECKey());
                }
            }
        };
        walletAppKit.startAsync();
        walletAppKit.awaitRunning();
        return walletAppKit.wallet();
    }

    private File workingDirectory() {
        return new File(walletDirectory);
    }

    @Bean
    public NetworkParameters networkParameters() {
        switch (network){
            case "testnet":
                return TestNet3Params.get();
            default:
                throw new RuntimeException("Specify what network you would like to use");
        }
    }
}
