(ns bill-mx.macros)

(defmacro when-let*
  "when-let w/ multiple bindings"
  ([bindings & body]
   (if (seq bindings)
     `(when-let [~(first bindings) ~(second bindings)]
        (when-let* ~(drop 2 bindings) ~@body))
     `(do ~@body))))