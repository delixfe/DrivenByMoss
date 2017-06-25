// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.push;

import de.mossgrabers.apc.command.trigger.BrowserCommand;
import de.mossgrabers.framework.Model;
import de.mossgrabers.framework.command.Commands;
import de.mossgrabers.framework.command.SceneCommand;
import de.mossgrabers.framework.command.aftertouch.AftertouchAbstractPlayViewCommand;
import de.mossgrabers.framework.command.continuous.FootswitchCommand;
import de.mossgrabers.framework.command.continuous.KnobRowModeCommand;
import de.mossgrabers.framework.command.continuous.MasterVolumeCommand;
import de.mossgrabers.framework.command.continuous.PlayPositionCommand;
import de.mossgrabers.framework.command.continuous.TempoCommand;
import de.mossgrabers.framework.command.trigger.ButtonRowModeCommand;
import de.mossgrabers.framework.command.trigger.CursorCommand.Direction;
import de.mossgrabers.framework.command.trigger.DeleteCommand;
import de.mossgrabers.framework.command.trigger.DuplicateCommand;
import de.mossgrabers.framework.command.trigger.KnobRowTouchModeCommand;
import de.mossgrabers.framework.command.trigger.MetronomeCommand;
import de.mossgrabers.framework.command.trigger.NewCommand;
import de.mossgrabers.framework.command.trigger.PlayCommand;
import de.mossgrabers.framework.command.trigger.RecordCommand;
import de.mossgrabers.framework.command.trigger.StopClipCommand;
import de.mossgrabers.framework.command.trigger.TapTempoCommand;
import de.mossgrabers.framework.command.trigger.UndoCommand;
import de.mossgrabers.framework.controller.AbstractControllerExtension;
import de.mossgrabers.framework.controller.DefaultValueChanger;
import de.mossgrabers.framework.controller.color.ColorManager;
import de.mossgrabers.framework.daw.AbstractTrackBankProxy;
import de.mossgrabers.framework.daw.CursorClipProxy;
import de.mossgrabers.framework.daw.CursorDeviceProxy;
import de.mossgrabers.framework.daw.EffectTrackBankProxy;
import de.mossgrabers.framework.daw.TrackBankProxy;
import de.mossgrabers.framework.daw.TransportProxy;
import de.mossgrabers.framework.daw.data.ChannelData;
import de.mossgrabers.framework.daw.data.SlotData;
import de.mossgrabers.framework.daw.data.TrackData;
import de.mossgrabers.framework.midi.MidiInput;
import de.mossgrabers.framework.midi.MidiOutput;
import de.mossgrabers.framework.mode.ModeManager;
import de.mossgrabers.framework.view.AbstractSequencerView;
import de.mossgrabers.framework.view.SceneView;
import de.mossgrabers.framework.view.View;
import de.mossgrabers.framework.view.ViewManager;
import de.mossgrabers.push.command.continuous.ConfigurePitchbendCommand;
import de.mossgrabers.push.command.continuous.MastertrackTouchCommand;
import de.mossgrabers.push.command.continuous.SmallKnobTouchCommand;
import de.mossgrabers.push.command.pitchbend.PitchbendCommand;
import de.mossgrabers.push.command.pitchbend.PitchbendSessionCommand;
import de.mossgrabers.push.command.trigger.AccentCommand;
import de.mossgrabers.push.command.trigger.AddEffectCommand;
import de.mossgrabers.push.command.trigger.AddTrackCommand;
import de.mossgrabers.push.command.trigger.AutomationCommand;
import de.mossgrabers.push.command.trigger.ClipCommand;
import de.mossgrabers.push.command.trigger.ConvertCommand;
import de.mossgrabers.push.command.trigger.DeviceCommand;
import de.mossgrabers.push.command.trigger.DoubleCommand;
import de.mossgrabers.push.command.trigger.FixedLengthCommand;
import de.mossgrabers.push.command.trigger.LayoutCommand;
import de.mossgrabers.push.command.trigger.MastertrackCommand;
import de.mossgrabers.push.command.trigger.MuteCommand;
import de.mossgrabers.push.command.trigger.OctaveCommand;
import de.mossgrabers.push.command.trigger.PageLeftCommand;
import de.mossgrabers.push.command.trigger.PageRightCommand;
import de.mossgrabers.push.command.trigger.PanSendCommand;
import de.mossgrabers.push.command.trigger.PushCursorCommand;
import de.mossgrabers.push.command.trigger.QuantizeCommand;
import de.mossgrabers.push.command.trigger.ScalesCommand;
import de.mossgrabers.push.command.trigger.SelectCommand;
import de.mossgrabers.push.command.trigger.SelectPlayViewCommand;
import de.mossgrabers.push.command.trigger.SelectSessionViewCommand;
import de.mossgrabers.push.command.trigger.SetupCommand;
import de.mossgrabers.push.command.trigger.ShiftCommand;
import de.mossgrabers.push.command.trigger.SoloCommand;
import de.mossgrabers.push.command.trigger.TrackCommand;
import de.mossgrabers.push.command.trigger.VolumeCommand;
import de.mossgrabers.push.controller.PushColors;
import de.mossgrabers.push.controller.PushControlSurface;
import de.mossgrabers.push.controller.PushDisplay;
import de.mossgrabers.push.controller.PushMidiInput;
import de.mossgrabers.push.mode.AccentMode;
import de.mossgrabers.push.mode.AutomationMode;
import de.mossgrabers.push.mode.ConfigurationMode;
import de.mossgrabers.push.mode.FixedMode;
import de.mossgrabers.push.mode.FrameMode;
import de.mossgrabers.push.mode.GrooveMode;
import de.mossgrabers.push.mode.InfoMode;
import de.mossgrabers.push.mode.Modes;
import de.mossgrabers.push.mode.NoteMode;
import de.mossgrabers.push.mode.NoteViewSelectMode;
import de.mossgrabers.push.mode.RibbonMode;
import de.mossgrabers.push.mode.ScaleLayoutMode;
import de.mossgrabers.push.mode.ScalesMode;
import de.mossgrabers.push.mode.SessionViewSelectMode;
import de.mossgrabers.push.mode.SetupMode;
import de.mossgrabers.push.mode.TransportMode;
import de.mossgrabers.push.mode.device.DeviceBrowserMode;
import de.mossgrabers.push.mode.device.DeviceLayerMode;
import de.mossgrabers.push.mode.device.DeviceLayerModePan;
import de.mossgrabers.push.mode.device.DeviceLayerModeSend;
import de.mossgrabers.push.mode.device.DeviceLayerModeVolume;
import de.mossgrabers.push.mode.device.DeviceParamsMode;
import de.mossgrabers.push.mode.track.ClipMode;
import de.mossgrabers.push.mode.track.CrossfaderMode;
import de.mossgrabers.push.mode.track.LayerDetailsMode;
import de.mossgrabers.push.mode.track.MasterMode;
import de.mossgrabers.push.mode.track.PanMode;
import de.mossgrabers.push.mode.track.SendMode;
import de.mossgrabers.push.mode.track.TrackDetailsMode;
import de.mossgrabers.push.mode.track.TrackMode;
import de.mossgrabers.push.mode.track.VolumeMode;
import de.mossgrabers.push.view.ClipView;
import de.mossgrabers.push.view.ColorView;
import de.mossgrabers.push.view.DrumView;
import de.mossgrabers.push.view.DrumView4;
import de.mossgrabers.push.view.DrumView64;
import de.mossgrabers.push.view.DrumView8;
import de.mossgrabers.push.view.PianoView;
import de.mossgrabers.push.view.PlayView;
import de.mossgrabers.push.view.PrgChangeView;
import de.mossgrabers.push.view.RaindropsView;
import de.mossgrabers.push.view.ScenePlayView;
import de.mossgrabers.push.view.SequencerView;
import de.mossgrabers.push.view.SessionView;
import de.mossgrabers.push.view.Views;

import com.bitwig.extension.controller.api.ControllerHost;


/**
 * Bitwig Studio extension to support the Ableton Push 1 and Push 2 controllers.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class PushControllerExtension extends AbstractControllerExtension<PushControlSurface, PushConfiguration>
{
    final boolean isPush2;


    /**
     * Constructor.
     *
     * @param extensionDefinition The extension definition
     * @param host The Bitwig host
     * @param isPush2 True if Push 2
     */
    protected PushControllerExtension (final PushControllerExtensionDefinition extensionDefinition, final ControllerHost host, final boolean isPush2)
    {
        super (extensionDefinition, host);
        this.isPush2 = isPush2;
        this.colorManager = new ColorManager ();
        PushColors.addColors (this.colorManager, isPush2);
        this.valueChanger = new DefaultValueChanger (1024, 10, 1);
        this.configuration = new PushConfiguration (this.valueChanger, isPush2);
    }


    /** {@inheritDoc} */
    @Override
    public void flush ()
    {
        this.surface.flush ();

        this.updateButtons ();
        this.updateMode (this.surface.getModeManager ().getActiveModeId ());

        final View activeView = this.surface.getViewManager ().getActiveView ();
        if (activeView == null)
            return;
        final de.mossgrabers.framework.command.core.PitchbendCommand pitchbendCommand = activeView.getPitchbendCommand ();
        if (pitchbendCommand != null)
            pitchbendCommand.updateValue ();
    }


    /** {@inheritDoc} */
    @Override
    protected void createModel ()
    {
        this.model = new Model (this.getHost (), this.colorManager, this.valueChanger, this.scales, 8, 8, this.isPush2 ? 8 : 6, this.isPush2 ? 48 : 16, this.isPush2 ? 48 : 16, false, -1, -1, -1, -1);

        final TrackBankProxy trackBank = this.model.getTrackBank ();
        trackBank.setIndication (true);
        trackBank.addTrackSelectionObserver (this::handleTrackChange);
        this.model.getMasterTrack ().addTrackSelectionObserver ( (index, isSelected) -> {
            if (isSelected)
                this.surface.getModeManager ().setActiveMode (Modes.MODE_MASTER);
            else
                this.surface.getModeManager ().restoreMode ();
        });
    }


    /** {@inheritDoc} */
    @Override
    protected void createSurface ()
    {
        final ControllerHost host = this.getHost ();
        final MidiOutput output = new MidiOutput (host);
        final MidiInput input = new PushMidiInput (this.isPush2);
        this.surface = new PushControlSurface (host, this.colorManager, this.configuration, output, input);
        final PushDisplay display = new PushDisplay (host, this.isPush2, this.valueChanger.getUpperBound (), output);
        display.setCommunicationPort (this.configuration.getSendPort ());
        this.surface.setDisplay (display);
        this.surface.getModeManager ().setDefaultMode (Modes.MODE_TRACK);
    }


    /** {@inheritDoc} */
    @Override
    protected void createModes ()
    {
        final ModeManager modeManager = this.surface.getModeManager ();

        modeManager.registerMode (Modes.MODE_VOLUME, new VolumeMode (this.surface, this.model));
        modeManager.registerMode (Modes.MODE_PAN, new PanMode (this.surface, this.model));
        modeManager.registerMode (Modes.MODE_CROSSFADER, new CrossfaderMode (this.surface, this.model));

        final SendMode modeSend = new SendMode (this.surface, this.model);
        modeManager.registerMode (Modes.MODE_SEND1, modeSend);
        modeManager.registerMode (Modes.MODE_SEND2, modeSend);
        modeManager.registerMode (Modes.MODE_SEND3, modeSend);
        modeManager.registerMode (Modes.MODE_SEND4, modeSend);
        modeManager.registerMode (Modes.MODE_SEND5, modeSend);
        modeManager.registerMode (Modes.MODE_SEND6, modeSend);

        modeManager.registerMode (Modes.MODE_MASTER, new MasterMode (this.surface, this.model, false));
        modeManager.registerMode (Modes.MODE_MASTER_TEMP, new MasterMode (this.surface, this.model, true));

        modeManager.registerMode (Modes.MODE_TRACK, new TrackMode (this.surface, this.model));
        modeManager.registerMode (Modes.MODE_TRACK_DETAILS, new TrackDetailsMode (this.surface, this.model));
        modeManager.registerMode (Modes.MODE_DEVICE_LAYER_DETAILS, new LayerDetailsMode (this.surface, this.model));
        modeManager.registerMode (Modes.MODE_CLIP, new ClipMode (this.surface, this.model));
        modeManager.registerMode (Modes.MODE_NOTE, new NoteMode (this.surface, this.model));
        modeManager.registerMode (Modes.MODE_FRAME, new FrameMode (this.surface, this.model));
        modeManager.registerMode (Modes.MODE_SCALES, new ScalesMode (this.surface, this.model));
        modeManager.registerMode (Modes.MODE_SCALE_LAYOUT, new ScaleLayoutMode (this.surface, this.model));
        modeManager.registerMode (Modes.MODE_ACCENT, new AccentMode (this.surface, this.model));
        modeManager.registerMode (Modes.MODE_FIXED, new FixedMode (this.surface, this.model));
        modeManager.registerMode (Modes.MODE_RIBBON, new RibbonMode (this.surface, this.model));
        modeManager.registerMode (Modes.MODE_GROOVE, new GrooveMode (this.surface, this.model));
        modeManager.registerMode (Modes.MODE_VIEW_SELECT, new NoteViewSelectMode (this.surface, this.model));
        modeManager.registerMode (Modes.MODE_SESSION_VIEW_SELECT, new SessionViewSelectMode (this.surface, this.model));

        modeManager.registerMode (Modes.MODE_AUTOMATION, new AutomationMode (this.surface, this.model));
        modeManager.registerMode (Modes.MODE_TRANSPORT, new TransportMode (this.surface, this.model));

        modeManager.registerMode (Modes.MODE_DEVICE_PARAMS, new DeviceParamsMode (this.surface, this.model));
        modeManager.registerMode (Modes.MODE_DEVICE_LAYER, new DeviceLayerMode (this.surface, this.model));

        modeManager.registerMode (Modes.MODE_BROWSER, new DeviceBrowserMode (this.surface, this.model));

        modeManager.registerMode (Modes.MODE_DEVICE_LAYER_VOLUME, new DeviceLayerModeVolume (this.surface, this.model));
        modeManager.registerMode (Modes.MODE_DEVICE_LAYER_PAN, new DeviceLayerModePan (this.surface, this.model));
        final DeviceLayerModeSend modeLayerSend = new DeviceLayerModeSend (this.surface, this.model);
        modeManager.registerMode (Modes.MODE_DEVICE_LAYER_SEND1, modeLayerSend);
        modeManager.registerMode (Modes.MODE_DEVICE_LAYER_SEND2, modeLayerSend);
        modeManager.registerMode (Modes.MODE_DEVICE_LAYER_SEND3, modeLayerSend);
        modeManager.registerMode (Modes.MODE_DEVICE_LAYER_SEND4, modeLayerSend);
        modeManager.registerMode (Modes.MODE_DEVICE_LAYER_SEND5, modeLayerSend);
        modeManager.registerMode (Modes.MODE_DEVICE_LAYER_SEND6, modeLayerSend);

        if (this.isPush2)
        {
            modeManager.registerMode (Modes.MODE_SEND7, modeSend);
            modeManager.registerMode (Modes.MODE_SEND8, modeSend);
            modeManager.registerMode (Modes.MODE_DEVICE_LAYER_SEND7, modeLayerSend);
            modeManager.registerMode (Modes.MODE_DEVICE_LAYER_SEND8, modeLayerSend);
            modeManager.registerMode (Modes.MODE_SETUP, new SetupMode (this.surface, this.model));
            modeManager.registerMode (Modes.MODE_INFO, new InfoMode (this.surface, this.model));
        }
        else
            modeManager.registerMode (Modes.MODE_CONFIGURATION, new ConfigurationMode (this.surface, this.model));
    }


    /** {@inheritDoc} */
    @Override
    protected void createObservers ()
    {
        if (this.configuration.isPush2 ())
        {
            this.configuration.addSettingObserver (PushConfiguration.DISPLAY_BRIGHTNESS, this.surface::sendDisplayBrightness);
            this.configuration.addSettingObserver (PushConfiguration.LED_BRIGHTNESS, this.surface::sendLEDBrightness);
            this.configuration.addSettingObserver (PushConfiguration.PAD_SENSITIVITY, () -> {
                this.surface.sendPadVelocityCurve ();
                this.surface.sendPadThreshold ();
            });
            this.configuration.addSettingObserver (PushConfiguration.PAD_GAIN, () -> {
                this.surface.sendPadVelocityCurve ();
                this.surface.sendPadThreshold ();
            });
            this.configuration.addSettingObserver (PushConfiguration.PAD_DYNAMICS, () -> {
                this.surface.sendPadVelocityCurve ();
                this.surface.sendPadThreshold ();
            });
        }
        else
        {
            this.configuration.addSettingObserver (PushConfiguration.VELOCITY_CURVE, this.surface::sendPadSensitivity);
            this.configuration.addSettingObserver (PushConfiguration.PAD_THRESHOLD, this.surface::sendPadSensitivity);
        }

        this.surface.getModeManager ().addModeListener ( (oldMode, newMode) -> {
            this.updateMode (null);
            this.updateMode (newMode);
        });

        this.surface.getViewManager ().addViewChangeListener ( (previousViewId, activeViewId) -> {
            // Update button states
            final View view = this.surface.getViewManager ().getActiveView ();
            for (final int button: this.surface.getButtons ())
            {
                if (this.surface.shouldUpdateButton (button))
                    this.surface.setButton (button, view.usesButton (button) ? ColorManager.BUTTON_STATE_ON : ColorManager.BUTTON_STATE_OFF);
            }
            // Update ribbon mode
            if (Views.VIEW_SESSION.equals (activeViewId))
                this.surface.setRibbonMode (PushControlSurface.PUSH_RIBBON_PAN);
            else
                this.updateRibbonMode ();
        });

        this.configuration.addSettingObserver (PushConfiguration.RIBBON_MODE, this::updateRibbonMode);
        this.configuration.addSettingObserver (PushConfiguration.SEND_PORT, () -> ((PushDisplay) this.surface.getDisplay ()).setCommunicationPort (this.configuration.getSendPort ()));

        this.createScaleObservers (this.configuration);
    }


    /** {@inheritDoc} */
    @Override
    protected void createViews ()
    {
        final ViewManager viewManager = this.surface.getViewManager ();
        viewManager.registerView (Views.VIEW_PLAY, new PlayView (this.surface, this.model));
        viewManager.registerView (Views.VIEW_SESSION, new SessionView (this.surface, this.model));
        viewManager.registerView (Views.VIEW_SEQUENCER, new SequencerView (this.surface, this.model));
        viewManager.registerView (Views.VIEW_DRUM, new DrumView (this.surface, this.model));
        viewManager.registerView (Views.VIEW_DRUM4, new DrumView4 (this.surface, this.model));
        viewManager.registerView (Views.VIEW_DRUM8, new DrumView8 (this.surface, this.model));
        viewManager.registerView (Views.VIEW_DRUM64, new DrumView64 (this.surface, this.model));
        viewManager.registerView (Views.VIEW_RAINDROPS, new RaindropsView (this.surface, this.model));
        viewManager.registerView (Views.VIEW_PIANO, new PianoView (this.surface, this.model));
        viewManager.registerView (Views.VIEW_PRG_CHANGE, new PrgChangeView (this.surface, this.model));
        viewManager.registerView (Views.VIEW_CLIP, new ClipView (this.surface, this.model));
        viewManager.registerView (Views.VIEW_COLOR, new ColorView (this.surface, this.model));
        viewManager.registerView (Views.VIEW_SCENE_PLAY, new ScenePlayView (this.surface, this.model));
    }


    /** {@inheritDoc} */
    @Override
    protected void registerTriggerCommands ()
    {
        final ViewManager viewManager = this.surface.getViewManager ();
        this.addTriggerCommand (Commands.COMMAND_PLAY, PushControlSurface.PUSH_BUTTON_PLAY, new PlayCommand<> (this.model, this.surface));
        this.addTriggerCommand (Commands.COMMAND_RECORD, PushControlSurface.PUSH_BUTTON_RECORD, new RecordCommand<> (this.model, this.surface));
        this.addTriggerCommand (Commands.COMMAND_NEW, PushControlSurface.PUSH_BUTTON_NEW, new NewCommand<> (this.model, this.surface));
        this.addTriggerCommand (Commands.COMMAND_DUPLICATE, PushControlSurface.PUSH_BUTTON_DUPLICATE, new DuplicateCommand<> (this.model, this.surface));
        this.addTriggerCommand (Commands.COMMAND_AUTOMATION, PushControlSurface.PUSH_BUTTON_AUTOMATION, new AutomationCommand (this.model, this.surface));
        this.addTriggerCommand (Commands.COMMAND_FIXED_LENGTH, PushControlSurface.PUSH_BUTTON_FIXED_LENGTH, new FixedLengthCommand (this.model, this.surface));
        this.addTriggerCommand (Commands.COMMAND_QUANTIZE, PushControlSurface.PUSH_BUTTON_QUANTIZE, new QuantizeCommand (this.model, this.surface));
        this.addTriggerCommand (Commands.COMMAND_DELETE, PushControlSurface.PUSH_BUTTON_DELETE, new DeleteCommand<> (this.model, this.surface));
        this.addTriggerCommand (Commands.COMMAND_DOUBLE, PushControlSurface.PUSH_BUTTON_DOUBLE, new DoubleCommand (this.model, this.surface));
        this.addTriggerCommand (Commands.COMMAND_UNDO, PushControlSurface.PUSH_BUTTON_UNDO, new UndoCommand<> (this.model, this.surface));
        this.addTriggerCommand (Commands.COMMAND_DEVICE, PushControlSurface.PUSH_BUTTON_DEVICE, new DeviceCommand (this.model, this.surface));
        this.addTriggerCommand (Commands.COMMAND_BROWSE, PushControlSurface.PUSH_BUTTON_BROWSE, new BrowserCommand<> (Modes.MODE_BROWSER, this.model, this.surface));
        this.addTriggerCommand (Commands.COMMAND_TRACK, PushControlSurface.PUSH_BUTTON_TRACK, new TrackCommand (this.model, this.surface));
        this.addTriggerCommand (Commands.COMMAND_CLIP, PushControlSurface.PUSH_BUTTON_CLIP, new ClipCommand (this.model, this.surface));
        this.addTriggerCommand (Commands.COMMAND_VOLUME, PushControlSurface.PUSH_BUTTON_VOLUME, new VolumeCommand (this.model, this.surface));
        this.addTriggerCommand (Commands.COMMAND_PAN_SEND, PushControlSurface.PUSH_BUTTON_PAN_SEND, new PanSendCommand (this.model, this.surface));

        for (int i = 0; i < 8; i++)
        {
            this.addTriggerCommand (Integer.valueOf (Commands.COMMAND_ROW1_1.intValue () + i), PushControlSurface.PUSH_BUTTON_ROW1_1 + i, new ButtonRowModeCommand<> (0, i, this.model, this.surface));
            this.addTriggerCommand (Integer.valueOf (Commands.COMMAND_ROW2_1.intValue () + i), PushControlSurface.PUSH_BUTTON_ROW2_1 + i, new ButtonRowModeCommand<> (1, i, this.model, this.surface));
            this.addTriggerCommand (Integer.valueOf (Commands.COMMAND_SCENE1.intValue () + i), PushControlSurface.PUSH_BUTTON_SCENE1 + i, new SceneCommand<> (i, this.model, this.surface));
        }

        this.addTriggerCommand (Commands.COMMAND_SHIFT, PushControlSurface.PUSH_BUTTON_SHIFT, new ShiftCommand (this.model, this.surface));
        this.addTriggerCommand (Commands.COMMAND_LAYOUT, PushControlSurface.PUSH_BUTTON_LAYOUT, new LayoutCommand (this.model, this.surface));
        this.addTriggerCommand (Commands.COMMAND_SELECT, PushControlSurface.PUSH_BUTTON_SELECT, new SelectCommand (this.model, this.surface));
        this.addTriggerCommand (Commands.COMMAND_TAP_TEMPO, PushControlSurface.PUSH_BUTTON_TAP, new TapTempoCommand<> (this.model, this.surface));
        this.addTriggerCommand (Commands.COMMAND_METRONOME, PushControlSurface.PUSH_BUTTON_METRONOME, new MetronomeCommand<> (this.model, this.surface));
        this.addTriggerCommand (Commands.COMMAND_MASTERTRACK, PushControlSurface.PUSH_BUTTON_MASTER, new MastertrackCommand (this.model, this.surface));
        this.addTriggerCommand (Commands.COMMAND_STOP_CLIP, PushControlSurface.PUSH_BUTTON_CLIP_STOP, new StopClipCommand<> (this.model, this.surface));
        this.addTriggerCommand (Commands.COMMAND_PAGE_LEFT, PushControlSurface.PUSH_BUTTON_DEVICE_LEFT, new PageLeftCommand (this.model, this.surface));
        this.addTriggerCommand (Commands.COMMAND_PAGE_RIGHT, PushControlSurface.PUSH_BUTTON_DEVICE_RIGHT, new PageRightCommand (this.model, this.surface));
        this.addTriggerCommand (Commands.COMMAND_MUTE, PushControlSurface.PUSH_BUTTON_MUTE, new MuteCommand (this.model, this.surface));
        this.addTriggerCommand (Commands.COMMAND_SOLO, PushControlSurface.PUSH_BUTTON_SOLO, new SoloCommand (this.model, this.surface));
        this.addTriggerCommand (Commands.COMMAND_SCALES, PushControlSurface.PUSH_BUTTON_SCALES, new ScalesCommand (this.model, this.surface));
        this.addTriggerCommand (Commands.COMMAND_ACCENT, PushControlSurface.PUSH_BUTTON_ACCENT, new AccentCommand (this.model, this.surface));
        this.addTriggerCommand (Commands.COMMAND_ADD_EFFECT, PushControlSurface.PUSH_BUTTON_ADD_EFFECT, new AddEffectCommand (this.model, this.surface));
        this.addTriggerCommand (Commands.COMMAND_ADD_TRACK, PushControlSurface.PUSH_BUTTON_ADD_TRACK, new AddTrackCommand (this.model, this.surface));
        this.addTriggerCommand (Commands.COMMAND_SELECT_PLAY_VIEW, PushControlSurface.PUSH_BUTTON_NOTE, new SelectPlayViewCommand (this.model, this.surface));
        this.addTriggerCommand (Commands.COMMAND_SELECT_SESSION_VIEW, PushControlSurface.PUSH_BUTTON_SESSION, new SelectSessionViewCommand (this.model, this.surface));
        this.addTriggerCommand (Commands.COMMAND_ARROW_DOWN, PushControlSurface.PUSH_BUTTON_DOWN, new PushCursorCommand (Direction.DOWN, this.model, this.surface));
        this.addTriggerCommand (Commands.COMMAND_ARROW_UP, PushControlSurface.PUSH_BUTTON_UP, new PushCursorCommand (Direction.UP, this.model, this.surface));
        this.addTriggerCommand (Commands.COMMAND_ARROW_LEFT, PushControlSurface.PUSH_BUTTON_LEFT, new PushCursorCommand (Direction.LEFT, this.model, this.surface));
        this.addTriggerCommand (Commands.COMMAND_ARROW_RIGHT, PushControlSurface.PUSH_BUTTON_RIGHT, new PushCursorCommand (Direction.RIGHT, this.model, this.surface));
        this.addTriggerCommand (Commands.COMMAND_OCTAVE_DOWN, PushControlSurface.PUSH_BUTTON_OCTAVE_DOWN, new OctaveCommand (false, this.model, this.surface));
        this.addTriggerCommand (Commands.COMMAND_OCTAVE_UP, PushControlSurface.PUSH_BUTTON_OCTAVE_UP, new OctaveCommand (true, this.model, this.surface));

        viewManager.registerTriggerCommand (Commands.COMMAND_SETUP, new SetupCommand (this.isPush2, this.model, this.surface));
        if (this.isPush2)
        {
            this.surface.assignTriggerCommand (PushControlSurface.PUSH_BUTTON_SETUP, Commands.COMMAND_SETUP);
            this.addTriggerCommand (Commands.COMMAND_CONVERT, PushControlSurface.PUSH_BUTTON_CONVERT, new ConvertCommand (this.model, this.surface));
        }
        else
            this.surface.assignTriggerCommand (PushControlSurface.PUSH_BUTTON_USER_MODE, Commands.COMMAND_SETUP);
    }


    /** {@inheritDoc} */
    @Override
    protected void registerContinuousCommands ()
    {
        for (int i = 0; i < 8; i++)
            this.addContinuousCommand (Integer.valueOf (Commands.CONT_COMMAND_KNOB1.intValue () + i), PushControlSurface.PUSH_KNOB1 + i, new KnobRowModeCommand<> (i, this.model, this.surface));

        this.addContinuousCommand (Commands.CONT_COMMAND_MASTER_KNOB, PushControlSurface.PUSH_KNOB9, new MasterVolumeCommand<> (this.model, this.surface));
        this.addContinuousCommand (Commands.CONT_COMMAND_TEMPO, PushControlSurface.PUSH_SMALL_KNOB1, new TempoCommand<> (this.model, this.surface));
        this.addContinuousCommand (Commands.CONT_COMMAND_PLAY_POSITION, PushControlSurface.PUSH_SMALL_KNOB2, new PlayPositionCommand<> (this.model, this.surface));
        this.addContinuousCommand (Commands.COMMAND_FOOTSWITCH, PushControlSurface.PUSH_FOOTSWITCH2, new FootswitchCommand<> (this.model, this.surface));

        this.addNoteCommand (Commands.CONT_COMMAND_KNOB1_TOUCH, PushControlSurface.PUSH_KNOB1_TOUCH, new KnobRowTouchModeCommand<> (0, this.model, this.surface));
        this.addNoteCommand (Commands.CONT_COMMAND_KNOB2_TOUCH, PushControlSurface.PUSH_KNOB2_TOUCH, new KnobRowTouchModeCommand<> (1, this.model, this.surface));
        this.addNoteCommand (Commands.CONT_COMMAND_KNOB3_TOUCH, PushControlSurface.PUSH_KNOB3_TOUCH, new KnobRowTouchModeCommand<> (2, this.model, this.surface));
        this.addNoteCommand (Commands.CONT_COMMAND_KNOB4_TOUCH, PushControlSurface.PUSH_KNOB4_TOUCH, new KnobRowTouchModeCommand<> (3, this.model, this.surface));
        this.addNoteCommand (Commands.CONT_COMMAND_KNOB5_TOUCH, PushControlSurface.PUSH_KNOB5_TOUCH, new KnobRowTouchModeCommand<> (4, this.model, this.surface));
        this.addNoteCommand (Commands.CONT_COMMAND_KNOB6_TOUCH, PushControlSurface.PUSH_KNOB6_TOUCH, new KnobRowTouchModeCommand<> (5, this.model, this.surface));
        this.addNoteCommand (Commands.CONT_COMMAND_KNOB7_TOUCH, PushControlSurface.PUSH_KNOB7_TOUCH, new KnobRowTouchModeCommand<> (6, this.model, this.surface));
        this.addNoteCommand (Commands.CONT_COMMAND_KNOB8_TOUCH, PushControlSurface.PUSH_KNOB8_TOUCH, new KnobRowTouchModeCommand<> (7, this.model, this.surface));
        this.addNoteCommand (Commands.CONT_COMMAND_TEMPO_TOUCH, PushControlSurface.PUSH_SMALL_KNOB1_TOUCH, new SmallKnobTouchCommand (this.model, this.surface));
        this.addNoteCommand (Commands.CONT_COMMAND_PLAYCURSOR_TOUCH, PushControlSurface.PUSH_SMALL_KNOB2_TOUCH, new SmallKnobTouchCommand (this.model, this.surface));
        this.addNoteCommand (Commands.CONT_COMMAND_CONFIGURE_PITCHBEND, PushControlSurface.PUSH_RIBBON_TOUCH, new ConfigurePitchbendCommand (this.model, this.surface));
        this.addNoteCommand (Commands.CONT_COMMAND_MASTERTRACK_TOUCH, PushControlSurface.PUSH_KNOB9_TOUCH, new MastertrackTouchCommand (this.model, this.surface));

        final ViewManager viewManager = this.surface.getViewManager ();
        viewManager.registerPitchbendCommand (new PitchbendCommand (this.model, this.surface));

        final PlayView playView = (PlayView) viewManager.getView (Views.VIEW_PLAY);
        playView.registerAftertouchCommand (new AftertouchAbstractPlayViewCommand<> (playView, this.model, this.surface));
        final PlayView pianoView = (PlayView) viewManager.getView (Views.VIEW_PIANO);
        pianoView.registerAftertouchCommand (new AftertouchAbstractPlayViewCommand<> (pianoView, this.model, this.surface));

        final SessionView sessionView = (SessionView) viewManager.getView (Views.VIEW_SESSION);
        sessionView.registerPitchbendCommand (new PitchbendSessionCommand (this.model, this.surface));
    }


    /** {@inheritDoc} */
    @Override
    protected void startup ()
    {
        this.getHost ().scheduleTask ( () -> {
            this.surface.getViewManager ().setActiveView (Views.VIEW_PLAY);
            this.surface.getModeManager ().setActiveMode (Modes.MODE_TRACK);
        }, 200);
    }


    private void updateButtons ()
    {
        final TransportProxy t = this.model.getTransport ();
        this.surface.updateButton (PushControlSurface.PUSH_BUTTON_METRONOME, t.isMetronomeOn () ? ColorManager.BUTTON_STATE_HI : ColorManager.BUTTON_STATE_ON);
        this.surface.updateButton (PushControlSurface.PUSH_BUTTON_PLAY, t.isPlaying () ? PushColors.PUSH_BUTTON_STATE_PLAY_HI : PushColors.PUSH_BUTTON_STATE_PLAY_ON);
        final boolean isFlipRecord = this.configuration.isFlipRecord ();
        this.surface.updateButton (PushControlSurface.PUSH_BUTTON_CLIP_STOP, this.surface.isPressed (PushControlSurface.PUSH_BUTTON_CLIP_STOP) ? PushColors.PUSH_BUTTON_STATE_STOP_HI : PushColors.PUSH_BUTTON_STATE_STOP_ON);

        final boolean isShift = this.surface.isShiftPressed ();
        final boolean isRecordShifted = isShift && !isFlipRecord || !isShift && isFlipRecord;
        if (isRecordShifted)
            this.surface.updateButton (PushControlSurface.PUSH_BUTTON_AUTOMATION, t.isWritingClipLauncherAutomation () ? PushColors.PUSH_BUTTON_STATE_REC_HI : PushColors.PUSH_BUTTON_STATE_REC_ON);
        else
            this.surface.updateButton (PushControlSurface.PUSH_BUTTON_AUTOMATION, t.isWritingArrangerAutomation () ? PushColors.PUSH_BUTTON_STATE_REC_HI : PushColors.PUSH_BUTTON_STATE_REC_ON);
        this.surface.updateButton (PushControlSurface.PUSH_BUTTON_RECORD, isRecordShifted ? t.isLauncherOverdub () ? PushColors.PUSH_BUTTON_STATE_OVR_HI : PushColors.PUSH_BUTTON_STATE_OVR_ON : t.isRecording () ? PushColors.PUSH_BUTTON_STATE_REC_HI : PushColors.PUSH_BUTTON_STATE_REC_ON);

        this.surface.updateButton (PushControlSurface.PUSH_BUTTON_ACCENT, this.configuration.isAccentActive () ? ColorManager.BUTTON_STATE_HI : ColorManager.BUTTON_STATE_ON);

        final PushConfiguration config = this.surface.getConfiguration ();
        if (this.isPush2)
        {
            final ModeManager modeManager = this.surface.getModeManager ();
            if (modeManager.isActiveMode (Modes.MODE_DEVICE_LAYER))
            {
                final CursorDeviceProxy cd = this.model.getCursorDevice ();
                final ChannelData layer = cd.getSelectedLayerOrDrumPad ();
                this.surface.updateButton (PushControlSurface.PUSH_BUTTON_MUTE, layer != null && layer.isMute () ? PushColors.PUSH_BUTTON_STATE_MUTE_HI : PushColors.PUSH_BUTTON_STATE_MUTE_ON);
                this.surface.updateButton (PushControlSurface.PUSH_BUTTON_SOLO, layer != null && layer.isSolo () ? PushColors.PUSH_BUTTON_STATE_SOLO_HI : PushColors.PUSH_BUTTON_STATE_SOLO_ON);
            }
            else
            {
                final AbstractTrackBankProxy tb = this.model.getCurrentTrackBank ();
                final TrackData selTrack = modeManager.isActiveMode (Modes.MODE_MASTER) ? this.model.getMasterTrack () : tb.getSelectedTrack ();
                this.surface.updateButton (PushControlSurface.PUSH_BUTTON_MUTE, selTrack != null && selTrack.isMute () ? PushColors.PUSH_BUTTON_STATE_MUTE_HI : PushColors.PUSH_BUTTON_STATE_MUTE_ON);
                this.surface.updateButton (PushControlSurface.PUSH_BUTTON_SOLO, selTrack != null && selTrack.isSolo () ? PushColors.PUSH_BUTTON_STATE_SOLO_HI : PushColors.PUSH_BUTTON_STATE_SOLO_ON);
            }

            this.surface.updateButton (PushControlSurface.PUSH_BUTTON_CONVERT, this.canConvertClip () ? ColorManager.BUTTON_STATE_ON : ColorManager.BUTTON_STATE_OFF);
        }
        else
        {
            final boolean isMuteState = config.isMuteState ();
            this.surface.updateButton (PushControlSurface.PUSH_BUTTON_MUTE, isMuteState ? PushColors.PUSH_BUTTON_STATE_MUTE_HI : PushColors.PUSH_BUTTON_STATE_MUTE_ON);
            this.surface.updateButton (PushControlSurface.PUSH_BUTTON_SOLO, !isMuteState ? PushColors.PUSH_BUTTON_STATE_SOLO_HI : PushColors.PUSH_BUTTON_STATE_SOLO_ON);
        }

        final ViewManager viewManager = this.surface.getViewManager ();
        final boolean isSessionView = Views.isSessionView (viewManager.getActiveViewId ());
        this.surface.updateButton (PushControlSurface.PUSH_BUTTON_NOTE, isSessionView ? ColorManager.BUTTON_STATE_ON : ColorManager.BUTTON_STATE_HI);
        this.surface.updateButton (PushControlSurface.PUSH_BUTTON_SESSION, isSessionView ? ColorManager.BUTTON_STATE_HI : ColorManager.BUTTON_STATE_ON);
        this.surface.updateButton (PushControlSurface.PUSH_BUTTON_ACCENT, config.isAccentActive () ? ColorManager.BUTTON_STATE_HI : ColorManager.BUTTON_STATE_ON);

        final View activeView = viewManager.getActiveView ();
        if (activeView != null)
        {
            ((PushCursorCommand) activeView.getTriggerCommand (Commands.COMMAND_ARROW_DOWN)).updateArrows ();
            ((SceneView) activeView).updateSceneButtons ();
        }

        final CursorClipProxy clip = activeView instanceof AbstractSequencerView && !(activeView instanceof ClipView) ? ((AbstractSequencerView<?, ?>) activeView).getClip () : null;
        this.surface.updateButton (PushControlSurface.PUSH_BUTTON_DEVICE_LEFT, clip != null && clip.canScrollStepsBackwards () ? ColorManager.BUTTON_STATE_ON : ColorManager.BUTTON_STATE_OFF);
        this.surface.updateButton (PushControlSurface.PUSH_BUTTON_DEVICE_RIGHT, clip != null && clip.canScrollStepsForwards () ? ColorManager.BUTTON_STATE_ON : ColorManager.BUTTON_STATE_OFF);
    }


    private void updateMode (final Integer mode)
    {
        if (mode == null)
            return;

        this.updateIndication (mode);

        final boolean isMasterOn = Modes.MODE_MASTER.equals (mode) || Modes.MODE_MASTER_TEMP.equals (mode) || Modes.MODE_FRAME.equals (mode);
        final boolean isVolumeOn = Modes.MODE_VOLUME.equals (mode) || Modes.MODE_CROSSFADER.equals (mode);
        final boolean isPanOn = mode.intValue () >= Modes.MODE_PAN.intValue () && mode.intValue () <= Modes.MODE_SEND8.intValue ();
        final boolean isDeviceOn = Modes.isDeviceMode (mode);
        boolean isMixOn = Modes.MODE_TRACK.equals (mode);
        if (this.isPush2)
            isMixOn = isMixOn || isVolumeOn || isPanOn;

        this.surface.updateButton (PushControlSurface.PUSH_BUTTON_MASTER, isMasterOn ? ColorManager.BUTTON_STATE_HI : ColorManager.BUTTON_STATE_ON);
        this.surface.updateButton (PushControlSurface.PUSH_BUTTON_TRACK, isMixOn ? ColorManager.BUTTON_STATE_HI : ColorManager.BUTTON_STATE_ON);
        this.surface.updateButton (PushControlSurface.PUSH_BUTTON_VOLUME, isVolumeOn ? ColorManager.BUTTON_STATE_HI : ColorManager.BUTTON_STATE_ON);
        this.surface.updateButton (PushControlSurface.PUSH_BUTTON_PAN_SEND, isPanOn ? ColorManager.BUTTON_STATE_HI : ColorManager.BUTTON_STATE_ON);
        this.surface.updateButton (PushControlSurface.PUSH_BUTTON_DEVICE, isDeviceOn ? ColorManager.BUTTON_STATE_HI : ColorManager.BUTTON_STATE_ON);
        this.surface.updateButton (PushControlSurface.PUSH_BUTTON_SCALES, Modes.MODE_SCALES.equals (mode) ? ColorManager.BUTTON_STATE_HI : ColorManager.BUTTON_STATE_ON);
        this.surface.updateButton (PushControlSurface.PUSH_BUTTON_FIXED_LENGTH, Modes.MODE_FIXED.equals (mode) ? ColorManager.BUTTON_STATE_HI : ColorManager.BUTTON_STATE_ON);
        this.surface.updateButton (PushControlSurface.PUSH_BUTTON_BROWSE, Modes.MODE_BROWSER.equals (mode) ? ColorManager.BUTTON_STATE_HI : ColorManager.BUTTON_STATE_ON);
        this.surface.updateButton (PushControlSurface.PUSH_BUTTON_CLIP, Modes.MODE_CLIP.equals (mode) ? ColorManager.BUTTON_STATE_HI : ColorManager.BUTTON_STATE_ON);

        if (this.isPush2)
            this.surface.updateButton (PushControlSurface.PUSH_BUTTON_SETUP, Modes.MODE_SETUP.equals (mode) ? ColorManager.BUTTON_STATE_HI : ColorManager.BUTTON_STATE_ON);
    }


    private void updateIndication (final Integer mode)
    {
        final TrackBankProxy tb = this.model.getTrackBank ();
        final EffectTrackBankProxy tbe = this.model.getEffectTrackBank ();
        final boolean isSession = this.surface.getViewManager ().isActiveView (Views.VIEW_SESSION);
        final boolean isEffect = this.model.isEffectTrackBankActive ();
        final boolean isPan = Modes.MODE_PAN.equals (mode);
        final boolean isVolume = Modes.MODE_VOLUME.equals (mode);

        tb.setIndication (!isEffect && isSession);
        tbe.setIndication (isEffect && isSession);

        final CursorDeviceProxy cursorDevice = this.model.getCursorDevice ();
        final TrackData selectedTrack = tb.getSelectedTrack ();
        for (int i = 0; i < tb.getNumTracks (); i++)
        {
            final boolean hasTrackSel = selectedTrack != null && selectedTrack.getIndex () == i && Modes.MODE_TRACK.equals (mode);
            tb.setVolumeIndication (i, !isEffect && (isVolume || hasTrackSel));
            tb.setPanIndication (i, !isEffect && (isPan || hasTrackSel));

            for (int j = 0; j < tb.getNumSends (); j++)
                tb.setSendIndication (i, j, !isEffect && (mode.intValue () - Modes.MODE_SEND1.intValue () == j || hasTrackSel));

            tbe.setVolumeIndication (i, isEffect);
            tbe.setPanIndication (i, isEffect && isPan);

            cursorDevice.getParameter (i).setIndication (true);
        }
    }


    /**
     * Handle a track selection change.
     *
     * @param index The index of the track
     * @param isSelected Has the track been selected?
     */
    private void handleTrackChange (final int index, final boolean isSelected)
    {
        if (!isSelected)
            return;

        final ViewManager viewManager = this.surface.getViewManager ();
        final ModeManager modeManager = this.surface.getModeManager ();

        // Recall last used view (if we are not in session mode)
        if (!viewManager.isActiveView (Views.VIEW_SESSION))
        {
            final TrackData selectedTrack = this.model.getCurrentTrackBank ().getSelectedTrack ();
            if (selectedTrack != null)
            {
                final Integer preferredView = viewManager.getPreferredView (selectedTrack.getPosition ());
                viewManager.setActiveView (preferredView == null ? Views.VIEW_PLAY : preferredView);
            }
        }

        if (modeManager.isActiveMode (Modes.MODE_MASTER))
            modeManager.setActiveMode (Modes.MODE_TRACK);

        if (viewManager.isActiveView (Views.VIEW_PLAY))
            viewManager.getActiveView ().updateNoteMapping ();

        // Reset drum octave because the drum pad bank is also reset
        this.scales.setDrumOctave (0);
        if (viewManager.isActiveView (Views.VIEW_DRUM))
            viewManager.getView (Views.VIEW_DRUM).updateNoteMapping ();
    }


    private boolean canConvertClip ()
    {
        final AbstractTrackBankProxy tb = this.model.getCurrentTrackBank ();
        final TrackData selectedTrack = tb.getSelectedTrack ();
        if (selectedTrack == null)
            return false;
        final SlotData [] slots = tb.getSelectedSlots (selectedTrack.getIndex ());
        if (slots.length == 0)
            return false;
        for (final SlotData slot: slots)
        {
            if (slot.hasContent ())
                return true;
        }
        return false;
    }


    private void updateRibbonMode ()
    {
        this.surface.setRibbonValue (0);

        switch (this.configuration.getRibbonMode ())
        {
            case PushConfiguration.RIBBON_MODE_CC:
            case PushConfiguration.RIBBON_MODE_FADER:
                this.surface.setRibbonMode (PushControlSurface.PUSH_RIBBON_VOLUME);
                break;

            default:
                this.surface.setRibbonMode (PushControlSurface.PUSH_RIBBON_PITCHBEND);
                break;
        }
    }
}
