package fi.csc.idp.stepup.impl;

import javax.annotation.Nonnull;
import javax.security.auth.Subject;

import org.opensaml.profile.context.ProfileRequestContext;

import net.shibboleth.idp.authn.AbstractValidationAction;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.authn.principal.UsernamePrincipal;

public class ValidateStepupAuthentication extends AbstractValidationAction {

    
    /** {@inheritDoc} */
    @Override
    protected void doExecute(
            @Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final AuthenticationContext authenticationContext) {
        buildAuthenticationResult(profileRequestContext, authenticationContext);
        return;
    }
    
    @Override
    protected Subject populateSubject(Subject subject) {
        //TODO:Get Subject from context and use it
        subject.getPrincipals().add(new UsernamePrincipal("dummy"));
        return subject;
    }

}
