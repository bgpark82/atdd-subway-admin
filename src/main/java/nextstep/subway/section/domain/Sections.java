package nextstep.subway.section.domain;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nextstep.subway.common.exception.ExistException;
import nextstep.subway.common.exception.NothingException;
import nextstep.subway.line.domain.Line;
import nextstep.subway.station.domain.Station;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Embeddable
public class Sections {

	@OneToMany(mappedBy = "line", cascade = CascadeType.ALL) //, orphanRemoval = true
	private final List<Section> sections = new ArrayList<>();

	@Transient
	private List<Station> stations;

	public Sections(Section section) {
		this.sections.add(section);
	}

	public static Sections of(Line line, Station upStation, Station downStation, int distance) {
		return new Sections(new Section(line, upStation, downStation, distance));
	}

	public void add(Section target) {
		validateSection(target);

		if (isExistUpStation(target)) {
			this.sections.stream()
				.filter(section -> section.isUpStation(target))
				.findFirst()
				.ifPresent(section -> section.updateUpStation(target));
			this.sections.add(target);
			return;
		}

		if (isExistDownStation(target)) {
			this.sections.stream()
				.filter(section -> section.isDownStation(target))
				.findFirst()
				.ifPresent(section -> section.updateDownStation(target));
			this.sections.add(target);
		}
	}

	public boolean isExistUpStation(Section section) {
		return isContainStation(section.getUpStation());
	}

	public boolean isExistDownStation(Section section) {
		return isContainStation(section.getDownStation());
	}

	public List<Station> getStations() {
		Set<Station> result = new LinkedHashSet<>();
		for (Section section : this.sections) {
			result.addAll(section.getStations());
		}
		return new ArrayList<>(result);
	}

	private void validateSection(Section target) {
		boolean upStationExist = isExistUpStation(target);
		boolean downStationExist = isExistDownStation(target);

		if (upStationExist && downStationExist) {
			throw new ExistException("이미 존재하는 구간입니다.");
		}

		if (!upStationExist && !downStationExist) {
			throw new NothingException("등록할 수 없는 구간입니다.");
		}
	}

	private boolean isContainStation(Station target) {
		return this.sections.stream()
			.anyMatch(station -> station.contains(target));
	}

}