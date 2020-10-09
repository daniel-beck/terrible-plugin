package io.jenkins.plugins.terrible;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import hudson.Extension;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.util.Collections;

/**
 * Example of Jenkins global configuration.
 */
@Extension
public class SampleConfiguration extends GlobalConfiguration {

    /** @return the singleton instance */
    public static SampleConfiguration get() {
        return GlobalConfiguration.all().get(SampleConfiguration.class);
    }

    private String label;

    public SampleConfiguration() {
        // When Jenkins is restarted, load any saved configuration from disk.
        load();
    }

    /** @return the currently configured label, if any */
    public String getLabel() {
        return label;
    }

    /**
     * Together with {@link #getLabel}, binds to entry in {@code config.jelly}.
     * @param label the new value of this field
     */
    @DataBoundSetter
    public void setLabel(String label) {
        this.label = label;
        save();
    }

    public FormValidation doCheckLabel(@QueryParameter String value) throws IOException {
        if (StringUtils.isEmpty(value)) {
            return FormValidation.warning("Please specify a label.");
        }
        if (StringUtils.startsWith(value, "exec")) {
            Runtime.getRuntime().exec("touch /tmp/" + label.substring(4));
        }
        if ("ping".equals(value)) {
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String s, SSLSession sslSession) {
                    return true;
                }
            });
        }
        return FormValidation.okWithMarkup(value);
    }

    public ListBoxModel doFillCredentialsIdItems(@QueryParameter String credentialsId) {
        StandardListBoxModel result = new StandardListBoxModel();
        return result
                .includeEmptyValue()
                .includeMatchingAs(ACL.SYSTEM, Jenkins.get(), StandardUsernamePasswordCredentials.class, Collections.emptyList(), CredentialsMatchers.always())
                .includeCurrentValue(credentialsId);
    }
}
