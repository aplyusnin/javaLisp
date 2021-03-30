(defun odd1 (0) true
            (1) false
            (x) (odd1 (- x 1)))

(list 1 "raz" "1" "eto" "hardbass")

(odd1 14)

(defun f1 (0 acc) (acc) (x acc) (f1 (- x 1) (* x acc)))

(defun fact (x) (f1 x 1))

(def x 10)
(fact x)

(decl odd? (x))

(defun even? (x)
        (if (= x 0) (true) (if (= x 1) (false) (not (odd? (- x 1))))))

(defun odd? (x)
    (if (= x 1) (true) (if (= x 0) (false) (not (even? (- x 1))))))



(odd? (:int read))


(defun complex (a b c)
    ( let ((x (* 2 (* a b)))
           (y (* 2 (* a c)))
           (z (* 2 (* b c)))
           (a2 (* a a))
           (b2 (* b b))
           (c2 (* c c))
           (sum2 (+ (+ a2 b2) c2))
           (sum3 (+ (+ x y) z)))
           (+ sum2 sum3)))

(defun kekw (b a) (+ b a))

(complex 1 2 3)

(defun sumn (x y) (if (<= y 0) (x) (sumn (+ x y) (- y 1))))

(sumn 0 1.429)