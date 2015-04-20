(ns crashverse.macros)

(defmacro logmaster [body]
  `(.log js/console (prn-str ~@body)))
