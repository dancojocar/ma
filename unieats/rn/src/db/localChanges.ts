import { notifyChanged } from "./changes";
import { getDb } from "./database";
import * as outboxDao from "./outboxDao";
import { discardPendingWithoutOps } from "./spotDao";

function discard(removeOps: () => number): boolean {
  let removed = 0;
  getDb().withTransactionSync(() => {
    removed = removeOps();
    discardPendingWithoutOps();
  });
  notifyChanged("spots", "outbox");
  return removed > 0;
}

/** Explicit sign-out: nothing queued on this device may replay under the next account. */
export function discardAllLocalChanges(): boolean {
  return discard(outboxDao.clear);
}

/** A session that ended without sign-out (401, biometric key invalidated) keeps its edits for the same user only. */
export function discardOtherUsersChanges(userId: string): boolean {
  return discard(() => outboxDao.removeOtherUsersOps(userId));
}
