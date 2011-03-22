package org.appwork.update.updateclient.stateapp;

import org.appwork.controlling.State;
import org.appwork.controlling.StateEvent;
import org.appwork.controlling.StateEventListener;
import org.appwork.controlling.StateMachine;
import org.appwork.controlling.StateMachineInterface;
import org.appwork.update.updateclient.StateAction;

public class StateApp implements StateEventListener {

    private StateMachine stateMachine;
    private StateAction  errorState;
    private StateAction  initState;
    private StateAction  endState;
    private Exception    exception;
    private StateAction  breakAfterPoint;
    private StateAction  breakBeforePoint;

    private StateAction  next;

    public StateApp() {

    }

    public StateAction getBreakAfterPoint() {
        return this.breakAfterPoint;
    }

    public StateAction getBreakBeforePoint() {
        return this.breakBeforePoint;
    }

    public Exception getException() {
        return this.exception;
    }

    public StateAction getNext() {
        return this.next;
    }

    public StateAction getState() {
        return (StateAction) this.stateMachine.getState();
    }

    public boolean hasPassed(final State state) {
        return this.stateMachine.hasPassed(state);
    }

    public void init(final StateAction init, final StateAction done, final StateAction error) {
        this.stateMachine = new StateMachine(new StateMachineInterface() {

            @Override
            public StateMachine getStateMachine() {

                return StateApp.this.stateMachine;
            }
        }, init, done);
        this.stateMachine.addListener(this);
        this.errorState = error;
        this.initState = init;
        this.endState = done;
    }

    public boolean isBreakPointed() {
        return this.breakAfterPoint == this.stateMachine.getState() || this.next == this.breakBeforePoint;
    }

    public boolean isFailed() {
        return this.stateMachine.hasPassed(this.errorState);
    }

    public boolean isFinal() {
        return this.stateMachine.getState().getChildren().size() == 0;
    }

    @Override
    public void onStateChange(final StateEvent event) {

        try {
            System.out.println(" ---------------->" + event.getNewState());

            ((StateAction) event.getNewState()).prepare();

            this.onStateEnter(((StateAction) event.getNewState()));
            this.next = ((StateAction) event.getNewState()).run();
            this.onStateExit(((StateAction) event.getNewState()));
            if (this.breakAfterPoint != null && this.breakAfterPoint == event.getNewState()) {
                System.out.println("<<-----Reached Desired BreakAfterpoint: " + event.getNewState());
                return;
            }
            if (this.breakBeforePoint != null && this.breakBeforePoint == this.next) {
                System.out.println("<<-----Reached Desired BreakAfterpoint: " + this.next);
                return;
            }
            if (this.next == null) { return; }
            this.stateMachine.setStatus(this.next);

        } catch (final Exception e) {

            this.exception = e;
            this.stateMachine.setStatus(this.errorState);
        }

    }

    public void onStateEnter(final StateAction newState) {

    }

    public void onStateExit(final StateAction stateAction) {

    }

    @Override
    public void onStateUpdate(final StateEvent event) {
        // TODO Auto-generated method stub

    }

    public void reset() {
        this.exception = null;
        this.stateMachine.forceState(this.initState);
    }

    public void resume() throws Exception {
        if (this.next == null) { return; }
        this.exception = null;
        this.stateMachine.setStatus(this.next);
        if (this.exception != null) { throw this.exception; }
    }

    public void setBreakAfterPoint(final StateAction breakAfterPoint) {
        this.breakAfterPoint = breakAfterPoint;
    }

    public void setBreakBeforePoint(final StateAction breakBeforePoint) {
        this.breakBeforePoint = breakBeforePoint;
    }

    public void start() throws Exception {
        this.reset();
        if (this.initState.getChildren().size() > 1) { throw new IllegalStateException("Init State should only have 1 child"); }
        this.stateMachine.setStatus(this.initState.getChildren().get(0));
        if (this.exception != null) { throw this.exception; }
    }

}
