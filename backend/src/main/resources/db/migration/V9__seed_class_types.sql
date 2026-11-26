-- SF-15: class types listed in the data model. Weights start at 1.00; an
-- administrator can change them later (SF-13, Q11), never by editing this file.
INSERT INTO class_types (code, name, hour_weight) VALUES
    ('LECTURE',  'wykład',       1.00),
    ('TUTORIAL', 'ćwiczenia',    1.00),
    ('LAB',      'laboratorium', 1.00),
    ('PROJECT',  'projekt',      1.00),
    ('SEMINAR',  'seminarium',   1.00);
