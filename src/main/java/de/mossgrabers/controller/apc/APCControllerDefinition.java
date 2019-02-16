// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.controller.apc;

import de.mossgrabers.framework.controller.DefaultControllerDefinition;
import de.mossgrabers.framework.utils.OperatingSystem;
import de.mossgrabers.framework.utils.Pair;

import java.util.Collections;
import java.util.List;
import java.util.UUID;


/**
 * Definition class for the Novation SL controller extension.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class APCControllerDefinition extends DefaultControllerDefinition
{
    private static final UUID EXTENSION_ID_MK_I  = UUID.fromString ("35E958A0-345F-11E4-8C21-0800200C9A66");
    private static final UUID EXTENSION_ID_MK_II = UUID.fromString ("14787D10-35DE-11E4-8C21-0800200C9A66");

    private final boolean     isMkII;


    /**
     * Constructor.
     *
     * @param isMkII True if is Mk II other Mk I
     */
    public APCControllerDefinition (final boolean isMkII)
    {
        super ("", "Jürgen Moßgraber", "5.20", isMkII ? EXTENSION_ID_MK_II : EXTENSION_ID_MK_I, isMkII ? "APC40 mkII" : "APC40", "Akai", 1, 1);
        this.isMkII = isMkII;
    }


    /** {@inheritDoc} */
    @Override
    public List<Pair<String [], String []>> getMidiDiscoveryPairs (final OperatingSystem os)
    {
        return Collections.singletonList (this.addDeviceDiscoveryPair (this.isMkII ? "APC40 mkII" : "Akai APC40"));
    }
}
